package kim.jeonghyeon.androidlibrary.extension

import android.app.Dialog
import android.content.DialogInterface
import android.view.WindowManager
import kim.jeonghyeon.kotlinlibrary.extension.ignoreException

fun Dialog.showWithoutException() {
    try {
        if (!isShowing) {
            show()
        }
    } catch (e: IllegalStateException) {

    } catch (e: WindowManager.BadTokenException) {

    }
}

fun DialogInterface.dismissWithoutException() {
    try {
        dismiss()
    } catch (e: IllegalStateException) {

    } catch (e: WindowManager.BadTokenException) {

    }
}


