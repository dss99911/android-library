package com.example.sampleandroid.view.model

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.example.sampleandroid.view.widget.SampleTextField
import kim.jeonghyeon.androidlibrary.compose.Screen
import kim.jeonghyeon.androidlibrary.compose.unaryPlus
import kim.jeonghyeon.androidlibrary.compose.widget.Button
import kim.jeonghyeon.androidlibrary.extension.resourceToString
import kim.jeonghyeon.sample.compose.R
import kim.jeonghyeon.sample.viewmodel.ApiAnnotationViewModel

@Composable
fun ApiAnnotationScreen(model: ApiAnnotationViewModel) {
    Screen(model) {
        Column {
            SampleTextField("Input value", model.input)
            Button("update") { model.onClick() }
            Text("current value : ${+model.result}")
        }
    }
}
// TODO reactive way.
//@Composable
//fun ApiAnnotationScreen2(model: ApiAnnotationViewModel2) {
//    Screen(model) {
//        Column {
//            SampleTextField("Input value", model.input)
//            Button("update", model.click)
//            Text("current value : ${+model.result}")
//        }
//    }
//}