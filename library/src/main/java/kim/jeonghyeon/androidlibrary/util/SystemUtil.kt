package kim.jeonghyeon.androidlibrary.util

import android.accounts.AccountManager
import android.content.Context
import java.util.ArrayList

object SystemUtil {
    fun getEmailList(context: Context): List<String> {
        val emailList = ArrayList<String>()
        val accounts = AccountManager.get(context).accounts
        for (account in accounts) {
            if (ValidationUtil.isValidEmail(account.name)) {
                emailList.add(account.name)
            }
        }
        return emailList
    }
}