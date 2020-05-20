package kim.jeonghyeon.androidlibrary.architecture.net.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class BaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //example. no need to do anything related to connection.
//        if (!NetworkUtil.isConnected()) {
//            throw NoNetworkError()
//        }
        return chain.proceed(chain.request())
    }
}