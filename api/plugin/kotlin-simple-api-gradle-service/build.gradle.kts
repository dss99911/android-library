plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
}

group = deps.simpleArch.api.gradleService.getGroupId()
version = deps.simpleArch.api.gradleService.getVersion()

dependencies {
//    implementation(project(":gradle-plugin:${deps.simpleArch.api.gradleServiceShared.getArtifactId()}"))
    implementation(deps.simpleArch.api.gradleServiceShared)
    compileOnly(deps.plugin.compilerEmbeddable)
    compileOnly(deps.plugin.auto)
    kapt(deps.plugin.auto)
}

publishJvm()