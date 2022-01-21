package com.common.network

import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import okhttp3.HttpUrl

import okhttp3.Cookie

import okhttp3.CookieJar

/**
 * Created by yan_x
 * @date 2020/11/19/019 16:45
 * @description
 */
class MyCookiesJar : CookieJar {
    // 持久化的存储 Cookie的类
    private val cookieStore: HashMap<String, List<Cookie>> = HashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies != null && cookies.isNotEmpty()) {
            val host = url.host
            for (item in cookies) {

                // 给 Webview 中传入Cookie
                CookieManager.getInstance().setAcceptCookie(true)
                //TODO ...ZJT  替换下面的代码
                CookieManager.getInstance().setCookie(
                    item.domain,
                    item.name + "=" + item.value + "; domain=" + item.domain + "; path=" + item.path
                )
                //                CookieManager.getInstance().setCookie(host, item.value());
            }
            cookieStore[host] = cookies as MutableList<Cookie>

            if (Build.VERSION.SDK_INT < 21) {
                CookieSyncManager.getInstance().sync()
            } else {
                CookieManager.getInstance().flush()
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host]?:ArrayList<Cookie>()
    }

    companion object {
        private const val TAG = "CookiesManager"
    }

}
