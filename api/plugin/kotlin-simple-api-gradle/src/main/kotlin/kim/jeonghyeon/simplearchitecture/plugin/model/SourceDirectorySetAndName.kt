package kim.jeonghyeon.simplearchitecture.plugin.model


import com.android.build.api.dsl.AndroidSourceSet
import kim.jeonghyeon.simplearchitecture.plugin.util.androidExtension
import kim.jeonghyeon.simplearchitecture.plugin.util.generatedSourceSetPath
import kim.jeonghyeon.simplearchitecture.plugin.util.multiplatformExtension
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KOTLIN_DSL_NAME
import org.jetbrains.kotlin.gradle.plugin.KOTLIN_JS_DSL_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


data class SourceDirectorySetAndName(val name: String, val sourceDirectorySet: SourceDirectorySet)

fun Project.getSourceDirectorySetAndNames(): List<SourceDirectorySetAndName> {

    // Multiplatform project.
    multiplatformExtension?.let {
        return it.sourceSets
            .filter {
                !it.name.contains(
                    "test",
                    true
                )
            }//ex) androidTest, androidTestDebug. androidTestRelease
            .map {
                SourceDirectorySetAndName(
                    it.name,
                    it.kotlin
                )
            }
    }

    // Android project.
    androidExtension?.let {
        return it.sourceSets
            .filter {
                !it.name.contains(
                    "test",
                    true
                )
            }//ex) androidTest, androidTestDebug. androidTestRelease
            .map {
                SourceDirectorySetAndName(
                    it.name,
                    it.kotlin!!
                )
            }
    }

    // Kotlin project.
    val sourceSets = property("sourceSets") as SourceSetContainer

    return listOf(
        SourceDirectorySetAndName(
            "main",
            sourceSets.getByName("main").kotlin!!
        )
    )
}

fun SourceDirectorySetAndName.addGeneratedSourceDirectory(project: Project) {
    sourceDirectorySet.srcDir(generatedSourceSetPath(project.buildDir.toString(), name))
}

private val AndroidSourceSet.kotlin: SourceDirectorySet?
    get() = kotlinSourceSet

private val SourceSet.kotlin: SourceDirectorySet?
    get() = kotlinSourceSet

private val Any.kotlinSourceSet: SourceDirectorySet?
    get() = (getKotlinSourceSet(KOTLIN_DSL_NAME) ?: getKotlinSourceSet(KOTLIN_JS_DSL_NAME))
        ?.kotlin

/**
 * `kotlinPlugin.javaClass.interfaces` has only 'org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet'
 * `convention.plugins` has only 'kotlin'
 */
private fun Any.getKotlinSourceSet(name: String): KotlinSourceSet? =
    (this as HasConvention).convention.plugins[name] as? KotlinSourceSet?

