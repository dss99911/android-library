package kim.jeonghyeon.common.net

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kim.jeonghyeon.common.ext.json
import kim.jeonghyeon.common.net.error.ApiError
import kim.jeonghyeon.common.net.error.ApiErrorBody
import kim.jeonghyeon.common.net.error.ApiErrorCode
import kim.jeonghyeon.common.net.error.isApiError
import kim.jeonghyeon.common.reflect.suspendProxy
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

fun httpClientDefault(config: (HttpClientConfig<*>) -> Unit = {}): HttpClient = HttpClient {
    install(JsonFeature) {
        //can't use kotlin serialization on request { body = arguments }
        //but gson can't be used on other platform except for jvm.
        serializer = GsonSerializer()
    }
    config(this)
}

inline fun <reified T> HttpClient.create(baseUrl: String) =
    suspendProxy(T::class.java) { method, arguments ->
        val mainPath = T::class.java.name.replace(".", "-")
        val subPath = method.name
        val baseUrlWithoutSlash = if (baseUrl.last() == '/') baseUrl.take(baseUrl.lastIndex) else baseUrl
        val returnType = method.kotlinFunction!!.returnType

        val response = try {
            post<HttpResponse>("$baseUrlWithoutSlash/$mainPath/$subPath") {
                contentType(ContentType.Application.Json)

                //can not use kotlin serialization.
                //type mismatch. required: capturedtype(out any). found: any
                body = arguments
            }
        } catch (e: Exception) {
            throwException(e)
        }
        validateResponse(response)

        response.readText().toJsonObject(returnType.javaType)
    }

fun throwException(e: Exception): Nothing {
    throw when (e) {
        is UnknownHostException, is SocketTimeoutException -> {
            ApiError(ApiErrorBody(ApiErrorCode.NO_NETWORK, "no network"), e)
        }
        else -> {
            ApiError(ApiErrorBody(ApiErrorCode.UNKNOWN, "unknown error"), e)
        }
    }
}


suspend fun validateResponse(response: HttpResponse) {
    //TODO HYUN [multi-platform2] : logger with timber
    //TODO HYUN [multi-platform2] : consider how to set header

    if (response.status.isSuccess()) {
        return
    }
    //TODO HYUN [multi-platform2] : if return value is unit. how to ignore the response body?

    if (response.status.isApiError()) {
        throw ApiError(response.readText().toJsonObject<ApiErrorBody>())
    }

    throw ApiError(ApiErrorBody(ApiErrorCode.UNKNOWN, "unknown error occurred"))
}

//TODO HYUN [multi-platform2] : remove and update library
fun <T> String.toJsonObject(type: Type): T = Gson().fromJson(this, type)
inline fun <reified T> String.toJsonObject(): T = Gson().fromJson(this, T::class.java)