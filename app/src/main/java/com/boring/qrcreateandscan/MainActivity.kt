package com.boring.qrcreateandscan

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.boring.qrcreateandscan.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pressTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        Log.d("QRApp", "앱 시작")

        activateBackgroundAnimation()

        createQR_Button.setOnClickListener {
            startActivity(Intent(this@MainActivity, CreateQRActivity::class.java))
            overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
        }
        scanQR_Button.setOnClickListener {
            startActivity(Intent(this@MainActivity, ScanQRActivity::class.java))
            overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
        }

    }

    private fun activateBackgroundAnimation() {
        val layout = main_layout
        val animationDrawable = layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(2000)
        animationDrawable.start()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressTime < 2000) {
            finish()
        }
        Toast.makeText(this, "한 번 더 취소버튼을 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        pressTime = System.currentTimeMillis()
    }
}
