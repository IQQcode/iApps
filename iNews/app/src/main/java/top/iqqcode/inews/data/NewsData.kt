package top.iqqcode.inews.data

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import top.iqqcode.inews.data.Article

/**
 * @Author: iqqcode
 * @Date: 2022-05-14 13:00
 * @Description:
 */
data class NewsData(

    val code: Int,

    val msg: String,

    val data: List<Article>,
)