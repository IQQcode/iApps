package top.iqqcode.inews


import android.annotation.SuppressLint
import android.content.Context
import org.litepal.LitePalApplication

/**
 * @Author: iqqcode
 * @Date: 2022-05-21 11:10
 * @Description:
 */
class NewsApplication : LitePalApplication() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        /**
         * API-KEY
         * https://www.mxnzp.com/doc/detail?id=12
         */
        const val API_KEY = "gspmleqpjxxr1gpa"

        /**
         * API_SECRET
         */
        const val API_SECRET = "bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09"
    }

    override fun onCreate() {
        super.onCreate()
        context = baseContext
    }
}