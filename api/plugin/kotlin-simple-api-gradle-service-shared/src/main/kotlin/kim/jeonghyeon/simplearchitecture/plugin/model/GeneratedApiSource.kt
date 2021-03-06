package kim.jeonghyeon.simplearchitecture.plugin.model

data class GeneratedApiSource(
    val fileName: String,//SampleApi.kt
    val name: String, //SampleApi
    val packageName: String, //kim.jeonghyeon.sample.api
    val sourceSetPath: String,
    val source: String
)