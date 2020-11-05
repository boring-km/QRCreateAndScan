package com.boring.qrcreateandscan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_scan_qr.*

class ScanQRActivity : AppCompatActivity() {

    private var qrScan: IntentIntegrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)

        scanning()

        rescan_Button.setOnClickListener {
            scanning()
        }
        back_Button.setOnClickListener {
            finish()
        }
    }

    private fun scanning() {
        qrScan = IntentIntegrator(this)
        qrScan!!.setOrientationLocked(false)
            .setBarcodeImageEnabled(true)
            .setPrompt("QR 코드 스캔")
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "인식못함", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.contents)))
            } catch (e: Exception) {
                Toast.makeText(this, "인터넷 연결 실패!\n클립보드에 읽어온 데이터를 저장합니다.", Toast.LENGTH_SHORT).show()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("qr_code",result.contents)
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(this,"저장된 데이터: $clipData", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)   // TODO: 사용 이유 알아내기
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
    }
}
