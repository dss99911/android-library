package kim.jeonghyeon.simplearchitecture.plugin

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.backend.common.descriptors.isSuspend
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable
import java.io.File

class ApiClassProcessor(
    val buildPath: String,
    val projectPath: String,
    val sourceSets: List<SourceSetOption>
) : ClassElementFindListener {

    val apiAnnotationName = Api::class.java.name

    override fun onClassElementFound(element: ClassElement) {
        //already processed classes come out several times
        // - different target, different build variant(debug, release)
        // - all the classes which can be referred by several target will be handled several times.
        if (element.isValid()) {
            element.createClassFile()
        }
    }

    private fun ClassElement.isValid(): Boolean = hasAnnotation(apiAnnotationName)
            && classDescriptor.modality == Modality.ABSTRACT//Todo limitation : can't detect if it's abstract class or interface
            && isTopLevelClass

    private fun ClassElement.createClassFile() {


        FileSpec.builder(packageName, getApiImplementationName(simpleName))
            .addComment("GENERATED by Simple Api Plugin")
            .addType(asClassSpec())
            .addFunction(getApiConstructorFunction(packageName, simpleName))
            .build()
            .writeTo(File(getGeneratedClassPath()))
    }

    fun ClassElement.getGeneratedClassPath(): String {
        //find target by matching source folder
        val sourceSetName = sourceSets.firstOrNull {
            it.sourcePathSet.any {
                path.startsWith(it)
            }
        }?.name ?: guessSourceSetName(path)

        return generatedFilePath(buildPath, sourceSetName)
    }

    fun getApiImplementationName(interfaceName: String) = interfaceName + "Impl"

    /**
     * fun SomeApi(client, baseUrl) {
     *    return SomApiImpl(client, baseUrl)
     * }
     */
    private fun getApiConstructorFunction(packageName: String, interfaceName: String) =
        FunSpec.builder(interfaceName)
            .addApiConstructorParameter()
            .returns(ClassName(packageName, interfaceName))
            .addStatement("return ${getApiImplementationName(interfaceName)}(client, baseUrl) ")
            .build()

    private fun FunSpec.Builder.addApiConstructorParameter(): FunSpec.Builder =
        addParameter(
            ParameterSpec.builder("client", ClassName("io.ktor.client", "HttpClient")).build()
        )
            .addParameter("baseUrl", String::class)

    private fun TypeSpec.Builder.addApiConstructorProperty(): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("client", ClassName("io.ktor.client", "HttpClient"))
                .initializer("client")
                .build()
        )
            .addProperty(
                PropertySpec.builder("baseUrl", String::class)
                    .initializer("baseUrl")
                    .build()
            )

    /**
     * !! LIMITATION !!
     * 1. for native, [ApiGradleSubplugin.apply] is called before 'ios', 'mobile' sourceSet is created.
     * 2. so, It's difficult to figure out which sourceSet a class belongs to on native
     * 3. in this case, we guess sourceSetName by path
     */
    private fun guessSourceSetName(path: String): String {
        val srcPath = "$projectPath/src/"
        if (!path.startsWith(srcPath)) {
            error("sourceSet is not recognized and also failed to guess : $path")
        }

        return path.replaceFirst(srcPath, "")
            .replaceAfter("/", "")
            .also {
                println("guessed : $it")
            }
    }

    fun ClassElement.asClassSpec(): TypeSpec {
        //classDescriptor member information
        // name : class's name
        // modality : ABSTRACT for interface and abstract class
        // module.simpleName() : "$module_$build_type" ex)common_release
        // containingDeclaration.name : package's last folder's name
        // containingDeclaration.fqNameSafe.asString() : package full name. ex) kim.jeonghyeon.simplearchitecture.plugin
        // containingDeclaration.platform : [{"targetVersion":"JVM_1_8","targetPlatformVersion":{},"platformName":"JVM"}] todo check if other platform?
        // containingDeclaration.parents.joinToString(","){it.name.asString() : "\u003ccommon_release\u003e". able to check it's build type debug or release
        // source.containingFile.name : file name including '.kt'

        return TypeSpec.classBuilder(ClassName(packageName, simpleName + "Impl"))
            .addSuperinterface(ClassName(packageName, simpleName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addApiConstructorParameter()
                    .build()
            )
            .addFunctions(functions()
                .filter { it.isValidFunction() }
                .map { it.asFunctionSpec(simpleName) }
            ).build()
    }

    fun CallableMemberDescriptor.isValidFunction(): Boolean = modality == Modality.ABSTRACT

    fun CallableMemberDescriptor.asFunctionSpec(className: String): FunSpec {
        //name : function's name
        //origin.name : same with `name`
        //source.containingFile.name : file name including '.kt'
        //valueParameters[0].type.toString() : parameter's type
        //returnType?.memberScope?.getFunctionNames()?.joinToString { it.asString() } : shows functions of the type
        //returnType?.memberScope?.getVariableNames()?.joinToString { it.asString() } : shows variables of the type
        check(isSuspend) { "@Api : abstract function should be suspend on $className.$name'" }

        val builder = FunSpec.builder(name.toString())
            .addModifiers(KModifier.SUSPEND)
            .addModifiers(KModifier.OVERRIDE)
            .addParameters(valueParameters.map { it.asParameterSpec() })
        returnType?.asTypeName()?.let {
            builder.returns(it)
        }

        return builder.build()
    }
}

private fun ValueParameterDescriptor.asParameterSpec(): ParameterSpec =
    ParameterSpec.builder(name.asString(), type.asTypeName()).build()

fun KotlinType.asTypeName(): TypeName {
    val className: ClassName = createClassName().let {
        if (isNullable()) it.copy(true) else it
    }

    if (arguments.isNotEmpty()) {
        return arguments
            .map { it.type.asTypeName() }
            .let { className.parameterizedBy(*it.toTypedArray()) }
    }
    return className
}

fun KotlinType.createClassName(): ClassName {
    //on Jvm, packageName is java.util, instead of kotlin, even if source set is common
    //todo currently only HashMap is checked.
    // need to check other standard classes
    if (packageName == "java.util") {
        if (name == "HashMap") {
            return ClassName("kotlin.collections", name)
        }
    }

    return ClassName(
        packageName, name
    )
}

val KotlinType.packageName: String get() = getJetTypeFqName(false).substringBeforeLast(".")
val KotlinType.name: String get() = getJetTypeFqName(false).substringAfterLast(".")