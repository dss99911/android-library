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

package kim.jeonghyeon.androidlibrary.testutil

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object LiveDataTestUtil {

    fun <T> getValue(liveData: LiveData<T>, seconds: Long = 2, count: Int = 1): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(count)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                latch.countDown()
                if (latch.count == 0L) {
                    data[0] = o
                    liveData.removeObserver(this)
                }
            }
        }
        liveData.observeForever(observer)
        latch.await(seconds, TimeUnit.SECONDS)

        @Suppress("UNCHECKED_CAST")
        return data[0] as T
    }
}