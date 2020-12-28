package kim.jeonghyeon.net

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kim.jeonghyeon.annotation.*
import kim.jeonghyeon.net.error.ApiError
import kim.jeonghyeon.net.error.ApiErrorBody
import kim.jeonghyeon.net.error.DeeplinkError
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

private val preControllers: MutableList<Any> = mutableListOf()

fun Application.addControllerBeforeInstallSimpleRouting(_controller: Any) {
    check(featureOrNull(SimpleRouting) == null) {
        "SimpleRouting should be installed after adding '${_controller::class.simpleName}'"
    }

    preControllers.add(_controller)
}

/**
 * TODO provide customization option for
 *  - StatusPages
 *  - ContentNegotiation : currently only support kotlinx serialization.
 *  - CallLogging
 */
class SimpleRouting(val config: Configuration) {
    class Configuration {
        @SimpleArchInternal
        val controllerList = mutableListOf(*preControllers.toTypedArray())
        var configure: (Routing.() -> Unit)? = null

        operator fun Any.unaryPlus() {
            controllerList.add(this)
        }

        @SimpleArchInternal
        var hasSignFeature = false//todo consider other way if there is additional customization required

    }

    companion object Feature : ApplicationFeature<Application, Configuration, SimpleRouting> {
        override val key: AttributeKey<SimpleRouting> = AttributeKey("Simple Api")

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): SimpleRouting {
            val config = Configuration().apply(configure)
            return SimpleRouting(config).apply { initialize(pipeline) }
        }
    }

    fun initialize(pipeline: Application) {
        pipeline.install(StatusPages) {
            val unknownException: suspend PipelineContext<Unit, ApplicationCall>.(exception: Throwable) -> Unit =
                {
                    println("[SimpleRouting] ${it.message}")
                    call.respond(
                        HttpStatusCode.ApiError,
                        ApiErrorBody(ApiErrorBody.CODE_UNKNOWN, it.message)
                    )
                }

            val apiException: suspend PipelineContext<Unit, ApplicationCall>.(exception: ApiError) -> Unit =
                {
                    println("[SimpleRouting] ${it.message}")
                    call.respond(HttpStatusCode.ApiError, it.body)
                }

            val deeplinkException: suspend PipelineContext<Unit, ApplicationCall>.(exception: DeeplinkError) -> Unit =
                {
                    println("[SimpleRouting] ${it.message}")
                    call.respond(HttpStatusCode.DeeplinkError, it.deeplinkInfo)
                }

            exception<Throwable> { error ->
                unknownException(error)
            }

            exception<ApiError> { error ->
                apiException(error)
            }

            exception<DeeplinkError> { error ->
                deeplinkException(error)
            }

            exception<InvocationTargetException> { error ->
                when (val targetException = error.targetException) {
                    is ApiError -> apiException(targetException)
                    is DeeplinkError -> deeplinkException(targetException)
                    else -> unknownException(targetException)
                }
            }

        }

        pipeline.install(ContentNegotiation) {
            json()
        }

        pipeline.install(Routing) {
            installControllers(config.controllerList)
            config.configure?.invoke(this)
        }

        pipeline.install(CallLogging)

    }

    private fun Routing.installControllers(controllers: List<Any>) {
        println("[SimpleRouting] Start to install Routing")
        controllers.forEach { installController(it) }
    }

    private fun Routing.installController(controller: Any) {

        controller::class.allSuperclasses
            .filter { it.getApiAnnotation() != null }
            .forEach { apiInterface ->
                val mainPath = apiInterface.getMainPath() ?: return@forEach
                installAuthenticate(apiInterface.annotations) {
                    println("[SimpleRouting] Main Path : $mainPath, ${controller::class.simpleName}")
                    route(mainPath) {
                        this.installSubPaths(controller, apiInterface)
                    }
                }
            }
    }

    private fun Route.installAuthenticate(annotations: List<Annotation>, build: Route.() -> Unit) {
        if (!config.hasSignFeature) {
            //ApiBindingController is added as default
            return build()
        }
        val authenticateAnnotation =
            annotations.filterIsInstance<Authenticate>().firstOrNull() ?: return build()
        if (authenticateAnnotation.name.isBlank()) {
            authenticate(build = build)
        } else {
            authenticate(authenticateAnnotation.name, build = build)
        }

    }

    private fun KClass<*>.getApiAnnotation(): Api? =
        annotations.firstOrNull { annotation -> annotation is Api } as? Api?

    private fun KClass<*>.getMainPath(): String? {
        //todo issue : can't differetiate empty path(root path only) and null path(package name + interface name).
        // so currently doesn't support empty path
        val path = getApiAnnotation()!!.path
        return when {
            path.isEmpty() -> java.name.replace(".", "/")
            path.isUri() -> null //ignore https:// path.
            else -> path
        }
    }

    private fun Route.installSubPaths(controller: Any, apiInterface: KClass<*>) {
        apiInterface.declaredFunctions.forEach { kfunction ->
            val subPath = kfunction.getSubPath() ?: return@forEach
            val method = kfunction.getMethod()

            installAuthenticate(kfunction.annotations) {
                route(subPath, method) {
                    println("[SimpleRouting]     Sub Path : $subPath $method")
                    handle {
                        handleRequest(controller, kfunction)
                    }
                }
            }
        }
    }

    private fun KFunction<*>.getSubPath(): String? {
        //todo issue : can't differetiate empty path(root path only) and null path(package name + interface name).
        // so currently doesn't support empty path
        val path = annotations.mapNotNull {
            it.getApiMethodPath()
        }.firstOrNull()

        return when {
            path.isNullOrEmpty() -> name
            path.isUri() -> null //ignore https:// path.
            else -> path
        }
    }

    private fun Annotation.getApiMethodPath(): String? = when (this) {
        is Get -> path
        is Post -> path
        is Put -> path
        is Delete -> path
        is Options -> path
        is Patch -> path
        else -> null
    }

    private fun KFunction<*>.getMethod(): HttpMethod {
        return annotations
            .filter { it.getApiMethodPath() != null }
            .mapNotNull {
                val methodName = it.annotationClass.simpleName?.toUpperCase(Locale.ENGLISH)
                    ?: return@mapNotNull null
                HttpMethod.parse(methodName)
            }.firstOrNull() ?: HttpMethod.Post
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleRequest(
        controller: Any,
        apiFunction: KFunction<*>
    ) {
        val controllerFunction = controller.findFunction(apiFunction)

        val body: JsonObject? = getBody()
        val args = apiFunction.parameters
            .subList(1, apiFunction.parameters.size)//first parameter is for suspend function
            .map { param ->
                getArgument(param, body)
            }.toTypedArray()

        val pipelineContextStore = PipelineContextStore(this)
        launch(coroutineContext + pipelineContextStore) {
            val response = controllerFunction.callSuspend(controller, *args)
            if (!pipelineContextStore.responded) {
                call.respond(
                    Json {}.encodeToJsonElement(
                        serializer(apiFunction.returnType),
                        response
                    )
                )
            }
        }.join()
    }

    @OptIn(InternalSerializationApi::class)
    private fun PipelineContext<Unit, ApplicationCall>.getArgument(
        param: KParameter,
        body: JsonObject?
    ): Any? {
        try {
            val json = Json { }

            val parameterAnnotation = param.annotations.firstOrNull { it.isParameterAnnotation() }
                ?: //if no annotation, it's body argument
                return body!![param.name]?.let {
                    json.decodeFromJsonElement(serializer(param.type), it)
                }

            return if (parameterAnnotation is Body) {
                body?.let { json.decodeFromJsonElement(serializer(param.type), it) }
            } else {
                when (parameterAnnotation) {
                    is Header -> call.request.headers[parameterAnnotation.name]
                    is Path -> call.parameters[parameterAnnotation.name]
                    is Query -> call.request.queryParameters[parameterAnnotation.name]
                    else -> null
                }?.let {
                    if (param.type.jvmErasure.let { it == String::class || it.java.isEnum }) {
                        json.encodeToJsonElement(String.serializer(), it).let {
                            //TEXT => "TEXT" => String or enum
                            json.decodeFromJsonElement(serializer(param.type), it)
                        }
                    } else {
                        json.decodeFromString(serializer(param.type), it)
                    }


                }
            }
        } catch (e: Exception) {
            println("[SimpleRouting]" + e.message + "\nparam : ${param.name}")
            throw e
        }
    }

    private fun Annotation.isParameterAnnotation(): Boolean =
        this is Header || this is Path || this is Query || this is Body

    private suspend fun PipelineContext<Unit, ApplicationCall>.getBody(): JsonObject? {
        //todo if body not exists, ignore it with better approach.
        return try {
            call.receive()
        } catch (e: ContentTransformationException) {
            e.printStackTrace()
            null
        }
    }

    private fun Any.findFunction(func: KFunction<*>): KFunction<*> =
        this::class.functions.first {
            func.name == it.name &&
                    func.parameters.mapIndexedNotNull { i, v -> if (i == 0) null else v.type } ==
                    it.parameters.mapIndexedNotNull { i, v -> if (i == 0) null else v.type }
        }
}