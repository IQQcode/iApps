package top.iqqcode.inews

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
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
import top.iqqcode.inews.view.ArticleDetailActivity
import top.iqqcode.inews.view.FootViewHolder.Companion.FAILED
import top.iqqcode.inews.view.FootViewHolder.Companion.HAS_MORE
import top.iqqcode.inews.view.NewsAdapter
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
 * Main activity
 * @constructor Create empty Main activity
 */
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

        // ??????1:???????????????????????????????????????????????????????????????UI???
        loadNewData(refresh)

        // ??????2???????????????
        onPullRefresh()
        // ??????3?????????????????????
        onLoadMore()
        onClick()
    }

    private fun loadNewData(loadType: Int) {
        if (isLoading) return
        isLoading = true
        if (isNetworkAvailable(NewsApplication.context)) {
            thread {
                val list = getDataFromNetwork()
                if (list != null && list.isNotEmpty()) {
                    runOnUiThread {
                        replaceDataInRecyclerView(list, loadType)
//                        thread {
//                            insertNewsToDataBase()
//                        }
                        mAdapter.setLoadState(HAS_MORE)
                        isLoading = false
                    }
                } else {
                    // ????????????????????????0????????????????????????????????????????????????
//                    val dataFromDatabase = getDataFromNetwork(6)
//                    // ??????UI
//                    activity?.runOnUiThread {
//                        replaceDataInRecyclerView(dataFromDatabase)
//                        newsAdapter.footerViewStatus = HAS_MORE
//                        isLoading = false
//                    }
                }
            }
        } else {
            mAdapter.setLoadState(FAILED)
            // ?????????????????????,?????????????????????????????????
        }
    }

    /**
     * ????????????
     */
    private fun onPullRefresh() {
        mRefreshLayout.setColorSchemeColors(Color.parseColor("#03A9F4"))
        mRefreshLayout.setOnRefreshListener {
            thread {
                Thread.sleep(700) // ????????????0.7???????????????????????????????????????????????????
                runOnUiThread {
                    loadNewData(refresh)
                    mRefreshLayout.isRefreshing = false // ???????????????????????????
                }
            }
        }
    }

    /**
     * ??????????????????
     */
    private fun onLoadMore() {
        // ????????????????????????
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // ????????????????????????????????????
            var isSlidingUpward = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as LinearLayoutManager?
                if (manager != null) {
                    // ???????????????
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // ?????????????????????????????????itemPosition
                            val lastItemPosition = manager.findLastCompletelyVisibleItemPosition()
                            val itemCount = manager.itemCount
                            // ????????????????????????????????????item????????????????????????
                            if (lastItemPosition == itemCount - 1 && isSlidingUpward) {
                                // ?????????????????????????????????????????????
                                loadNewData(load)
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
             * ??????SwipeRefreshLayout???RecyclerView??????????????????
             * @param recyclerView
             * @param dx
             * @param dy
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val topRowVerticalPosition =
                    if (recyclerView.childCount == 0)
                        0 else recyclerView.getChildAt(0).top
                mRefreshLayout.isEnabled = topRowVerticalPosition >= 0
                // ??????0???????????????????????????????????????0???????????????????????????
                /**
                 * ????????????????????????dy > 0
                 * ?????????????????????dx > 0???; ????????????????????????0 ????????????????????????????????????
                 */
                isSlidingUpward = dy > 0
            }
        })
    }

    /**
     * ????????????Item????????????
     *  https://www.mxnzp.com/api/news/details?newsId=EJA5MJQ30001875N&app_id=onxudwg6nriqlluz&app_secret=bjhSOVpJbTE5ZmUvSDYvak93cGt3QT09
     */
    private fun onClick() {
        // ??????item????????????
        mAdapter.setOnItemClickListener(object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int, articleData: Article?) {
                val newsId = articleData?.newsId
                val newsDetailUrl = ApiClient.getNewsDetails(newsId)
                val intent = Intent(this@MainActivity, ArticleDetailActivity::class.java)
                intent.putExtra("details_url", newsDetailUrl)
                startActivity(intent)
            }
        })
    }

    /**
     * ?????????UI
     *
     */
    private fun initView() {

        mRecyclerView = binding.mRecyclerView
        mRefreshLayout = binding.refreshLayout
        mRefreshLayout.setOnRefreshListener(this)
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private fun getDataFromNetwork(): List<Article>? {
        pageData++
        var dataList: List<Article>? = null
        val newsListUrl = ApiClient.getNewsData(pageData);
        val call = ApiClient.getApiClient(newsListUrl)
        val response = call.execute()
        try {
            val json = response.body()?.string()
            // ???json??????????????????java??????
            val responseData = Gson().fromJson(json, NewsData::class.java)
            if (responseData != null) {
                when (responseData.code) {
                    1 -> {
                        dataList = responseData.data
                    }
                    else -> {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "??????????????????", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // ?????????UI??????(??? ?????????) ????????????UI?????????
            runOnUiThread {
                Toast.makeText(this@MainActivity, "??????????????????", Toast.LENGTH_SHORT).show()
            }
        }
//        call.enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Toast.makeText(this@MainActivity, "??????????????????~", Toast.LENGTH_SHORT).show()
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


    /**
     * ???????????????
     *
     * @param menu
     * @return
     */
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
                    // TODO??????????????????query????????????
                    loadNewData(refresh)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadNewData(refresh)
                return false
            }
        })
        menuItem.icon.setVisible(false, false)
        return true
    }

    override fun onRefresh() {

    }

    /**
     * ??????UI??????:??? newData ????????? RecyclerView?????????????????????
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun replaceDataInRecyclerView(newData: List<Article>, loadType: Int) {
        try {
            if (loadType == refresh) {
                mNewsList.clear()
            }
            mNewsList.addAll(newData)
            mAdapter.setData(mNewsList)
            mAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun insertNewsToDataBase() {
        try {
            // ?????????????????????????????????????????? id??????
            for (i in mNewsList.size - 1 downTo 0) {
                // ????????????????????????????????????
                val news = mNewsList[i]
                val resultList = LitePal.where("title=?", news.title).find(Article::class.java)
                if (resultList.isEmpty()) {
                    // ???????????????????????????????????????????????????????????????????????????
                    news.save()
                } else {
                    // ????????????????????????????????????
                    news.id = resultList[0].id
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            // ?????????UI??????????????????UI?????????
            runOnUiThread {
                Toast.makeText(this@MainActivity, "??????????????????", Toast.LENGTH_SHORT).show()
            }
        }
    }
}