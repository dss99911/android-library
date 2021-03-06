package kim.jeonghyeon.simplearchitecture.plugin.config

import kim.jeonghyeon.simplearchitecture.plugin.DEPENDENCY_SIMPLE_ARCHITECTURE
import kim.jeonghyeon.simplearchitecture.plugin.extension.simpleArchExtension
import kim.jeonghyeon.simplearchitecture.plugin.task.getGenerateSimpleConfigTask
import kim.jeonghyeon.simplearchitecture.plugin.util.dependsOnCompileTask
import org.gradle.api.Project

/**
 * todo this is not working if set true on library and application both.
 * this is not working properly if there is library module. and application module and both use this plugin
 * so, set simpleConfig = false on other library. only base module set true.
 */
fun Project.applySimpleConfig() {
    afterEvaluate {//to perform after source set is initialized.
        if (!simpleArchExtension.simpleConfig) {
            return@afterEvaluate
        }

        dependsOnCompileTask { getGenerateSimpleConfigTask(it, simpleArchExtension.simpleProperties) }
    }
}