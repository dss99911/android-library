/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.util

import kim.jeonghyeon.androidlibrary.architecture.livedata.LiveObject
import kim.jeonghyeon.androidlibrary.architecture.livedata.LiveResource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> LiveObject<T>.await(count: Int = 1, seconds: Long = 2): T {
    val latch = CountDownLatch(count)
    observeForever {
        latch.countDown()
    }
    latch.await(seconds, TimeUnit.SECONDS)

    return value as T
}

fun <T> LiveResource<T>.awaitData(seconds: Long = 2): T {
    //if already value exists, countdown 2 times
    val oldValue = value
    val latch = CountDownLatch(1)
    observeForever {
        if (!it.isResult() || it.get() === oldValue) {
            return@observeForever
        }
        latch.countDown()
    }
    latch.await(seconds, TimeUnit.SECONDS)
    //todo throws if timeout? if value already exists, value will be wrong.
    return value!!.get() as T
}