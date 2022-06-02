package top.iqqcode.inews.data

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

/**
 * @Author: iqqcode
 * @Date: 2022-05-14 13:02
 * @Description:
 */
data class Article(

    // title-新闻标题
    var title: String,

    // imgList-新闻描述图片列表

    val imgList: MutableList<String>,

    // source-新闻来源
    var source: String,

    // newsId-新闻唯一id，后面查询新闻详情需要
    var newsId: String,

    //  digest-新闻摘要
    var digest: String,

    // postTime-新闻发布时间
    var postTime: String,
)