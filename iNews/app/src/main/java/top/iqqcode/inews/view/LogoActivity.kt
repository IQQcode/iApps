package top.iqqcode.inews.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import top.iqqcode.inews.MainActivity
import top.iqqcode.inews.databinding.ActivityLogoBinding

class LogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mMainButton.setOnClickListener {
            startActivity(Intent(this@LogoActivity, MainActivity::class.java))
        }
    }
}