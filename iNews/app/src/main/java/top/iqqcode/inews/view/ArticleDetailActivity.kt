package top.iqqcode.inews.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import top.iqqcode.inews.databinding.ActivityArticleDetailBinding
import kotlin.math.abs


class ArticleDetailActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityArticleDetailBinding
    private lateinit var mAppBarLayout: AppBarLayout
    private lateinit var mCollapsingToolbarLayout: CollapsingToolbarLayout

    private var isHideToolbarView: Boolean = false

    private lateinit var mDateContainer: FrameLayout
    private var mDetailUrl: String = ""

    @SuppressLint("ObsoleteSdkInt")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }


    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initBundle()

        // 获得ActionBar实例
//        val supportActionBar: ActionBar? = supportActionBar
//        supportActionBar?.hide()


        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "hahah"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mCollapsingToolbarLayout.title = "yayayay"

        mAppBarLayout.addOnOffsetChangedListener(this)
        initWebView(mDetailUrl)
    }

    private fun initBundle() {
        mDetailUrl = intent.getStringExtra("details_url").toString()
    }

    private fun initView() {
        mCollapsingToolbarLayout = binding.collapsingToolbar
        mAppBarLayout = binding.appbar
        mDateContainer = binding.dateBehaviorContainer
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val maxScroll = mAppBarLayout.totalScrollRange
        val percentage: Float = abs(verticalOffset) / maxScroll.toFloat()
        if (percentage == 1F && isHideToolbarView) {
            mDateContainer.visibility = View.GONE
            binding.titleAppbar.visibility = View.VISIBLE
        } else if (percentage < 1F && isHideToolbarView) {
            mDateContainer.visibility = View.VISIBLE
            binding.titleAppbar.visibility = View.GONE
        }
        isHideToolbarView = !isHideToolbarView
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        val webView = binding.webView
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }
}