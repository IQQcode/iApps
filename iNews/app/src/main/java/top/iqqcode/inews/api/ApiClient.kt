package top.iqqcode.inews.api

import android.annotation.SuppressLint
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import top.iqqcode.inews.NewsApplication
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * @Author: iqqcode
 * @Date: 2022-05-14 13:15
 * @Description:
 */
object ApiClient {

    // https://www.mxnzp.com/api/news/list?typeId=509&page=1&app_id=gspmleqpjxxr1gpa&app_secret=bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09
    private const val BASE_URL = "https://www.mxnzp.com/api/news/"

    private const val LIST_TYPE = "list"

    /**
     * 话题类型
     */
    private const val TYPE_ID = 509

    fun getApiClient(url: String): Call {
        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return httpClient.newCall(request)
    }

    fun getNewsData(page: Int): String {
        return BASE_URL + LIST_TYPE + "?typeId=" + TYPE_ID + "&page=" + page + getSecret()
    }

    private fun getSecret(): String {
        return "&app_id=" + NewsApplication.API_KEY + "&app_secret=" + NewsApplication.API_SECRET
    }

    /**
     * Kotlin绕过SSL验证
     * @link: https://www.jianshu.com/p/c5b1a2f64ef5
     * @return OkHttpClient.Builder
     * @throws CertificateException
     */
    fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>, authType: String,
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String,
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            })
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory: SSLSocketFactory? = sslContext.socketFactory

            val allHostsValid = HostnameVerifier { _, _ -> true }
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            sslSocketFactory?.let {
                builder.sslSocketFactory(it,
                    trustAllCerts[0] as X509TrustManager)
            }
            builder.hostnameVerifier(allHostsValid)
            return builder
            // 如果 hostname in certificate didn't match的话就给一个默认的主机验证
//            setDefaultSSLSocketFactory(sslContext.socketFactory);
//            setDefaultHostnameVerifier(allHostsValid);
//            return sslContext.socketFactory;
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}