package com.boring.qrcreateandscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
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
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_create_qr.*
import java.io.InputStream


@Suppress("DEPRECATION")
open class CreateQRActivity : AppCompatActivity() {

    private var qrImage: BitmapImage = NoneQRCodeImage()
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

        loadImage_button.setOnClickListener {
            selectImageFromGallery()
        }

        setEditTextAction()
    }

    private fun selectImageFromGallery() {
        if (qrImage is QRCodeImage) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE)
        } else {
            Toast.makeText(applicationContext, "QR 코드를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            loadImageOnQRCode(data)
        } else {
            Toast.makeText(applicationContext, "Canceled!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageOnQRCode(data: Intent?) {
        val inputStream: InputStream? = data!!.data?.let { contentResolver.openInputStream(it) }
        val selectedImage = BitmapFactory.decodeStream(inputStream)
        inputStream!!.close()
        bitmap = mergeBitmaps(selectedImage, bitmap)
        qrcode_ImageView.setImageBitmap(bitmap)
    }

    open fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap {
        val combined =
            Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
        val canvas = Canvas(combined)
        val canvasWidth: Int = canvas.width
        val canvasHeight: Int = canvas.height
        canvas.drawBitmap(qrcode, Matrix(), null)
        val resizeLogo =
            Bitmap.createScaledBitmap(logo!!, 50, 50, true)
        val centreX = (canvasWidth - resizeLogo.width) / 2
        val centreY = (canvasHeight - resizeLogo.height) / 2
        canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
        return combined
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
            if(qrImage.saveImage(bitmap, contentResolver))
                Toast.makeText(applicationContext, "저장완료", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(applicationContext, "실패", Toast.LENGTH_SHORT).show()
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

    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when {
            !clipboard.hasPrimaryClip() -> {}
            !clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN)!! -> {}
            else -> {
                val item = clipboard.primaryClip?.getItemAt(0)
                url_EditText.setText(item?.text.toString())
                create_Button.performClick()
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
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {}
                else -> {
                    Toast.makeText(applicationContext, "승인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createQRCodeImage() {
        val url = url_EditText.text.toString()
        val writer = QRCodeWriter()
        try {
            val matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300)
            val encoder = BarcodeEncoder()

            bitmap = encoder.createBitmap(matrix)
            qrImage = QRCodeImage()
            qrcode_ImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
    }
}
