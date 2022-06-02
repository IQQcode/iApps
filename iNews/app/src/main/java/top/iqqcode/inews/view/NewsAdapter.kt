package top.iqqcode.inews.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import top.iqqcode.inews.R
import top.iqqcode.inews.data.Article
import top.iqqcode.inews.util.UtilHelper

/**
 * @Author: iqqcode
 * @Date: 2022-05-18 23:29
 * @Description:
 */
class NewsAdapter(private val mContext: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 普通布局
    private val TYPE_NORMAL = 1

    // Footer布局
    private val TYPE_FOOTER = 2



    // 当前加载状态，默认为加载完成
    private var loadState = FootViewHolder.HAS_MORE

    private var mNewsData: List<Article>? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    /**
     * 为列表设置数据源
     * @param list List<Article>?
     */
    fun setData(list: List<Article>) {
        mNewsData = list
    }

    /**
     * 设置回调接口
     * @param clickListener OnItemClickListener?
     */
    fun setOnItemClickListener(clickListener: OnItemClickListener?) {
        mOnItemClickListener = clickListener
    }



    /**
     * 设置上拉加载状态
     * @param loadState
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(state: Int) {
        this.loadState = state
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 通过判断显示类型，来创建不同的View
        when (viewType) {
            TYPE_FOOTER -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_refresh_footer, parent, false)
                return FootViewHolder(view)
            }
            else -> {
                val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_card_view, parent, false)
                return CommonViewHolder(view)
            }
        }
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_NORMAL && holder is CommonViewHolder) {
            // 通过为条目设置点击事件触发回调
            holder.itemView.setOnClickListener {
                mOnItemClickListener!!.onItemClick(it, holder.adapterPosition)
            }

            val model: Article? = mNewsData?.get(position)
            Log.i("IQQCODE", "position: > \n" + model.toString())
            val requestOptions = RequestOptions()
            requestOptions.placeholder(UtilHelper.randomDrawbleColor)
            requestOptions.error(UtilHelper.randomDrawbleColor)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            requestOptions.centerCrop()
            val imageUrl = model?.imgList?.get(0)
            Glide.with(mContext)
                .load(imageUrl)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        holder.mProcessBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        holder.mProcessBar.visibility = View.GONE
                        return false
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.mCoverPage)

            holder.mTitle.text = model?.title
            if (model?.digest != null) {
                holder.mDesc.visibility = View.VISIBLE
                holder.mDesc.text = model.digest
            } else {
                holder.mDesc.visibility = View.GONE
            }
            holder.mSource.text = model?.source
            holder.mTime.text = model?.postTime
        } else if (getItemViewType(position) == TYPE_FOOTER && holder is FootViewHolder) {
            val holder: FootViewHolder = holder as FootViewHolder
            // 处理加载状态
            holder.updateLoadMore(holder.itemView, loadState)
            holder.itemView.setOnClickListener {
                // mLoadingView.visibility = View.VISIBLE
                // 将状态调整成HAS_MORE执行一次loadCacheData()
                // loadState = FootViewHolder.HAS_MORE
                Toast.makeText(mContext, "Has More Load Data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 列表项中增加了一个 footer_view,因此要+1
    override fun getItemCount(): Int = if (mNewsData == null) 0 else mNewsData!!.size

    /**
     * 判断第position条新闻应该用哪一种列表项展示，返回viewType
     * @param position Int
     * @return Int
     */
    override fun getItemViewType(position: Int): Int {
        // 最后一个item设置为FooterView
        return if (position + 1 == itemCount) {
            TYPE_FOOTER
        } else {
            TYPE_NORMAL
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) { // 当前是否为网格布局
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // 如果当前是footer的位置，那么该item占据2个单元格，正常情况下占据1个单元格
                    // 返回值决定了每个Item占据的单元格数
                    return if (getItemViewType(position) == TYPE_FOOTER) manager.spanCount else 1
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class CommonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @JvmField
        var mTitle: TextView = itemView.findViewById(R.id.newsTitle)
        var mDesc: TextView = itemView.findViewById(R.id.desc)
        var mAuthor: TextView = itemView.findViewById(R.id.author)
        var mSource: TextView = itemView.findViewById(R.id.source)
        var mTime: TextView = itemView.findViewById(R.id.time)
        var mCoverPage: ImageView = itemView.findViewById(R.id.itemImage)
        var mProcessBar: ProgressBar = itemView.findViewById(R.id.progressLoadPhoto)

    }
}