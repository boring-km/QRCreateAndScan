package com.boring.qrcreateandscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_create_qr.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
open class CreateQR : AppCompatActivity() {

    private var bitmap:Bitmap? = null
    private val requestNumber = 1001

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_qr)
        create_button.setOnClickListener {
            execute()
        }
        clipboard_button.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            when {
                !clipboard.hasPrimaryClip() -> { }
                !clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN)!! -> { }
                else -> {
                    val item = clipboard.primaryClip?.getItemAt(0)
                    input_url.setText(item?.text.toString())
                }
            }
        }

        save_button.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this@CreateQR,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this@CreateQR,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    }
                    else -> {
                        ActivityCompat.requestPermissions(this@CreateQR, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),requestNumber)
                        Toast.makeText(this, "QR 코드를 이미지로 저장하려면 권한 승인이 필요합니다!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if (bitmap != null) {
                    val date = Date(System.currentTimeMillis())
                    val time = SimpleDateFormat("yyyy_MM_dd_hh_mm").format(date)
                    saveImage(bitmap!!, time)
                } else {
                    Toast.makeText(this, "QR 코드를 먼저 생성하세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
        input_url.imeOptions = EditorInfo.IME_ACTION_DONE
        input_url.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save_button.performClick()
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            requestNumber -> {
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
                return
            }
        }
    }
    private fun execute() {
        val url = input_url.text.toString()
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val encoder = BarcodeEncoder()
            bitmap = encoder.createBitmap(matrix)
            // 파일명을 위한 시간 가져오기

            // QR 코드 이미지뷰에 보여주기
            qrcode.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveImage(bitmap: Bitmap, time: String) {

        // PNG 파일로 저장
        val imagePath = File(Environment.getExternalStorageDirectory().absolutePath + "/QR Codes")

        try {
            if (!imagePath.exists()) {
                imagePath.mkdirs()
                Log.d("TAG", "폴더 생성")
            } else {
                Log.d("TAG", "폴더 존재")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val finalPath = saveToInternalStorage(bitmap, time, imagePath)
        Log.d("log_path", finalPath.absolutePath)
        // 미디어 스캔 방법 1
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(finalPath))
        applicationContext.sendBroadcast(intent)
        Toast.makeText(this, "QR 코드 이미지가 저장됩니다", Toast.LENGTH_SHORT).show()
    }
    private fun saveToInternalStorage(bitmapImage: Bitmap, time: String, file: File): File {
        // path to /data/data/yourapp/app_data/imageDir
        //val directory = cw.getDir("image", Context.MODE_PRIVATE)



        Log.d("dir_tag", file.toString())
        // Create imageDir
        val mypath = File(file, "$time.png")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return mypath
    }
}
