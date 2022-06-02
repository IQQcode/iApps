package top.iqqcode.inews.view

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.iqqcode.inews.R

/**
 * @Author: iqqcode
 * @Date: 2022-05-21 16:20
 * @Description:
 */
class FootViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    protected var mLoadingView: LinearLayout
    protected var mNoMoreView: LinearLayout
    protected var mErrorView: TextView

    fun updateLoadMore(view: View, state: Int) {
        when (state) {
            HAS_MORE -> {
                mLoadingView.visibility = View.VISIBLE
                mNoMoreView.visibility = View.GONE
                mErrorView.visibility = View.GONE
            }
            FINISHED -> {
                mNoMoreView.visibility = View.VISIBLE
                mLoadingView.visibility = View.GONE
                mErrorView.visibility = View.GONE
            }
            FAILED -> {
                mErrorView.visibility = View.VISIBLE
                mLoadingView.visibility = View.GONE
                mNoMoreView.visibility = View.GONE
            }
            else -> {}
        }
    }

    init {
        mLoadingView = itemView.findViewById<View>(R.id.footer_loading) as LinearLayout
        mNoMoreView = itemView.findViewById<View>(R.id.footer_no_more) as LinearLayout
        mErrorView = itemView.findViewById<View>(R.id.footer_error) as TextView
    }

    // 定义 footer_view的几种可能状态
    companion object {

        /**
         * HAS_MORE状态：正在加载 - footer_view 的进度条转圈,可执行 loadCacheData()
         */
        const val HAS_MORE = 0x111

        /**
         * FINISHED状态：加载完成 - footer_view 显示"已经没有更多内容了",不可执行 loadCacheData()
         */
        const val FINISHED = 0x222

        /**
         * FAILED状态：加载到底 - footer_view 显示"加载失败,点击重新加载",不可执行 loadCacheData()
         */
        const val FAILED = 0x333
    }
}