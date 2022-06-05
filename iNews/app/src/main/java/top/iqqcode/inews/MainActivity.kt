package top.iqqcode.inews

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import org.litepal.LitePal
import top.iqqcode.inews.api.ApiClient
import top.iqqcode.inews.data.Article
import top.iqqcode.inews.data.NewsData
import top.iqqcode.inews.databinding.ActivityMainBinding
import top.iqqcode.inews.util.isNetworkAvailable
import top.iqqcode.inews.view.FootViewHolder
import top.iqqcode.inews.view.FootViewHolder.Companion.FAILED
import top.iqqcode.inews.view.FootViewHolder.Companion.FINISHED
import top.iqqcode.inews.view.FootViewHolder.Companion.HAS_MORE
import top.iqqcode.inews.view.NewsAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mAdapter: NewsAdapter

    private var mNewsList: MutableList<Article> = ArrayList()
    private var pageData: Int = 1
    private var isLoading = false
    
    companion object {
        private const val refresh = 1
        private const val load = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.isNestedScrollingEnabled = false
        mAdapter = NewsAdapter(this)
        mRecyclerView.adapter = mAdapter

        // 功能1:创建页面后立即从网络获取新的数据，并刷新到UI上
        loadNewData()

        // 功能2：下拉刷新
        onPullRefresh()
        // 功能3：上拉加载更多
        onLoadMore()
        onClick()
    }

    private fun loadNewData() {
        if (isLoading) return
        isLoading = true
        if (isNetworkAvailable(NewsApplication.context)) {
            thread {
                val list = getDataFromNetwork()
                if (list != null && list.isNotEmpty()) {
                    runOnUiThread {
                        replaceDataInRecyclerView(list)
//                        thread {
//                            insertNewsToDataBase()
//                        }
                        mAdapter.setLoadState(HAS_MORE)
                        isLoading = false
                    }
                } else {
                    // 如果从网络获取到0条数据，改从本地数据库中获取数据
//                    val dataFromDatabase = getDataFromNetwork(6)
//                    // 刷新UI
//                    activity?.runOnUiThread {
//                        replaceDataInRecyclerView(dataFromDatabase)
//                        newsAdapter.footerViewStatus = HAS_MORE
//                        isLoading = false
//                    }
                }
            }
        } else {
            mAdapter.setLoadState(FAILED)
            // 如果网络不可用,只能从数据库中获取数据
        }
    }

    /**
     * 下拉刷新
     */
    private fun onPullRefresh() {
        mRefreshLayout.setColorSchemeColors(Color.parseColor("#03A9F4"))
        mRefreshLayout.setOnRefreshListener {
            thread {
                Thread.sleep(700) // 这个延迟0.7秒只是为了实现视觉效果，与逻辑无关
                runOnUiThread {
                    loadNewData()
                    mRefreshLayout.isRefreshing = false // 让圆形进度条停下来
                }
            }
        }
    }

    /**
     * 上拉加载更多
     */
    private fun onLoadMore() {
        // 设置加载更多监听
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // 用来标记是否正在向上滑动
            var isSlidingUpward = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as LinearLayoutManager?
                if (manager != null) {
                    // 当不滑动时
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // 获取最后一个完全显示的itemPosition
                            val lastItemPosition = manager.findLastCompletelyVisibleItemPosition()
                            val itemCount = manager.itemCount
                            // 判断是否滑动到了最后一个item，并且是向上滑动
                            if (lastItemPosition == itemCount - 1 && isSlidingUpward) {
                                // 向下滑动到底部时，立即加载数据
                                loadNewData()
                            }
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> {

                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> {

                        }
                        else -> {}
                    }
                }
            }

            /**
             * 解决SwipeRefreshLayout和RecyclerView下拉刷新冲突
             * @param recyclerView
             * @param dx
             * @param dy
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val topRowVerticalPosition =
                    if (recyclerView == null || recyclerView.childCount === 0)
                        0 else recyclerView.getChildAt(0).top
                mRefreshLayout.isEnabled = topRowVerticalPosition >= 0
                // 大于0表示正在向上滑动，小于等于0表示停止或向下滑动
                /**
                 * 当向上滑动的时候dy > 0
                 * 向左滑动的时候dx > 0的; 反方向滑动则小于0 适用于横向滑动列表的监听
                 */
                isSlidingUpward = dy > 0
            }
        })
    }

    private fun onClick() {
        // 真正处理item点击事件
        mAdapter.setOnItemClickListener(object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(this@MainActivity, ArticleDetailActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun initView() {

        mRecyclerView = binding.mRecyclerView
        mRefreshLayout = binding.refreshLayout
        mRefreshLayout.setOnRefreshListener(this)
    }

    private fun getDataFromNetwork(): List<Article>? {
        pageData++
        var dataList: List<Article>? = null
        val newsListUrl = ApiClient.getNewsData(pageData);
        val call = ApiClient.getApiClient(newsListUrl)
        val response = call.execute()
        try {
            val json = response.body()?.string()
            // 将json字符串解析为java对象
            val responseData = Gson().fromJson(json, NewsData::class.java)
            if (responseData != null) {
                when (responseData.code) {
                    1 -> {
                        dataList = responseData.data
                    }
                    else -> {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "网络数据异常", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 切换回UI线程(即 主线程) 执行刷新UI的操作
            runOnUiThread {
                Toast.makeText(this@MainActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
            }
        }
//        call.enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Toast.makeText(this@MainActivity, "网络不给力呦~", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//
//            }
//        })
        val num = dataList?.size
        println(num)
        return dataList
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager: SearchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = menu?.findItem(R.id.action_search)?.actionView as SearchView
        val menuItem: MenuItem = menu.findItem(R.id.action_search)

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Search Latest News..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query?.length!! > 2) {
                    // TODO：输入关键字query查询新闻
                    loadNewData()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadNewData()
                return false
            }
        })
        menuItem.icon.setVisible(false, false)
        return true
    }

    override fun onRefresh() {

    }

    /**
     * 刷新UI操作:用 newData 替换掉 RecyclerView中所有的旧数据
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun replaceDataInRecyclerView(newData: List<Article>) {
        try {
//            if (loadType == refresh) {
//                mNewsList.clear()
//            }
            mNewsList.addAll(newData)
            mAdapter.setData(mNewsList)
            mAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun insertNewsToDataBase() {
        try {
            // 逆序插入的目的是让越早的新闻 id越小
            for (i in mNewsList.size - 1 downTo 0) {
                // 先在数据库中按标题查一遍
                val news = mNewsList[i]
                val resultList = LitePal.where("title=?", news.title).find(Article::class.java)
                if (resultList.isEmpty()) {
                    // 如果本地数据库中没有同一标题的新闻，就执行插入操作
                    news.save()
                } else {
                    // 如果已经有同一标题的新闻
                    news.id = resultList[0].id
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            // 切换回UI线程执行刷新UI的操作
            runOnUiThread {
                Toast.makeText(this@MainActivity, "数据缓存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}