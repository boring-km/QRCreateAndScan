package com.boring.qrcreateandscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.boring.qrcreateandscan.bitmap.BitmapImage
import com.boring.qrcreateandscan.bitmap.NoneQRCodeImage
import com.boring.qrcreateandscan.bitmap.QRCodeImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_create_qr.*


// TODO: QR 코드 이미지 객체를 따로 분리해야 할 지 살펴보기
@Suppress("DEPRECATION")
open class CreateQRActivity : AppCompatActivity() {

    private var pressTime: Long = 0
    private var bitmapImage: BitmapImage = NoneQRCodeImage()
    private lateinit var bitmap: Bitmap
    private val requestNumber = 1001

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_qr)
        create_Button.setOnClickListener {
            createQRCodeImage()
        }

        clipboard_Button.setOnClickListener {
            copyToClipboard()
        }

        save_Button.setOnClickListener {
            saveQRCodeImage()
        }

        setEditTextAction()
    }

    private fun setEditTextAction() {
        url_EditText.imeOptions = EditorInfo.IME_ACTION_DONE
        url_EditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save_Button.performClick()
            }
            return@setOnEditorActionListener false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveQRCodeImage() {
        if (isNotStorageWritePermitted()) {
            requestStorageWritePermission()
        } else {
            bitmapImage.saveImage(bitmap, contentResolver)
            Toast.makeText(applicationContext, "QR 코드를 저장합니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNotStorageWritePermitted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@CreateQRActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestStorageWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@CreateQRActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Log.d("파일 쓰기 권한", "권한 있음")
        } else {
            ActivityCompat.requestPermissions(
                this@CreateQRActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestNumber
            )
            Toast.makeText(this, "QR 코드를 이미지로 저장하려면 권한 승인이 필요합니다!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // TODO: when 내부의 의미 파악하기
    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when {
            !clipboard.hasPrimaryClip() -> {
            }
            !clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN)!! -> {
            }
            else -> {
                val item = clipboard.primaryClip?.getItemAt(0)
                url_EditText.setText(item?.text.toString())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestNumber) {
            when {
                grantResults.isEmpty() -> {
                    Toast.makeText(applicationContext, "비어있음", Toast.LENGTH_SHORT).show()
                }
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                }
                else -> {
                    Toast.makeText(applicationContext, "승인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createQRCodeImage() {
        val url = url_EditText.text.toString()
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val encoder = BarcodeEncoder()
            bitmap = encoder.createBitmap(matrix)
            bitmapImage = QRCodeImage()
            qrcode_ImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressTime < 2000) {
            overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
            finish()
        }
        Toast.makeText(this, "한 번 더 취소하시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        pressTime = System.currentTimeMillis()
    }

}
