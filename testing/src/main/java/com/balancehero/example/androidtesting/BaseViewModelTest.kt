package com.balancehero.example.androidtesting

import androidx.navigation.NavDirections
import kim.jeonghyeon.androidlibrary.architecture.mvvm.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

/**
 * whenever change test, application is created, so, koin module's also restarted.
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
abstract class BaseViewModelTest : BaseRobolectricTest() {

    inline fun <reified T : NavDirections> BaseViewModel.captureNavigateDirection(): T {
        if (!Mockito.mockingDetails(this).isSpy) {
            error("viewModel should be spy")
        }
        val argTask = argumentCaptor<NavDirections>()
        Mockito.verify(this).navigateDirection(capture(argTask))
        return argTask.value as T
    }
}