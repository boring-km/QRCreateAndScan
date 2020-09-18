package com.boring.qrcreateandscan

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var pressTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
