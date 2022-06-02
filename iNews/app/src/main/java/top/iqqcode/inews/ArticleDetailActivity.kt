package top.iqqcode.inews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import top.iqqcode.inews.databinding.ActivityArticleDetailBinding
import top.iqqcode.inews.databinding.ActivityMainBinding

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}