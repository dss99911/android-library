package com.example.sampleandroid.view.model

import androidx.compose.Composable
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.material.Button
import kim.jeonghyeon.androidlibrary.compose.widget.TextField
import kim.jeonghyeon.androidlibrary.extension.resourceToString
import kim.jeonghyeon.sample.compose.R
import kim.jeonghyeon.sample.viewmodel.ApiHeaderViewModel

class ApiHeaderScreen(private val model: ApiHeaderViewModel = ApiHeaderViewModel()) : ModelScreen(model) {
    override val title: String = R.string.header_call.resourceToString()

    @Composable
    override fun compose() {
        super.compose()
    }

    @Composable
    override fun view() {
        Column {
            Text("current header : ${+model.result}")
            TextField(model.input)
            Button(model::onClick) {
                Text("change header")
            }
        }
    }
}