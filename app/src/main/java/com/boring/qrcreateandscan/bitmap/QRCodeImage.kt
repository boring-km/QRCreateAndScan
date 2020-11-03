package com.boring.qrcreateandscan.bitmap

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class QRCodeImage : BitmapImage() {

    // TODO: Android Q Scoped Storage 학습 필요
    // 참고: https://choidev-1.tistory.com/73, https://hjiee.tistory.com/entry/Android-MediaStore로-화면-스크린샷-저장하기-Android-Q
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun saveImage(
        image: Bitmap,
        contentResolver: ContentResolver
    ): Boolean {
        val imageName = generateImageName()

        val values = createImageValues(imageName)
        val item: Uri? =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            val fileDescriptor: ParcelFileDescriptor =
                contentResolver.openFileDescriptor(item!!, "w", null)!!
            val inputStream = getImageInputStream(image)
            val stringToByte = getBytes(inputStream)
            val fos = FileOutputStream(fileDescriptor.fileDescriptor)

            fos.write(stringToByte)
            fos.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)   // Pending 해제
                contentResolver.update(item, values, null, null)
            }
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun generateImageName(): String {
        val dateTime = Date(System.currentTimeMillis())
        val formattedDateTime = SimpleDateFormat("yyyy_MM_dd_hh_mm").format(dateTime)
        return "$formattedDateTime.png"
    }

    private fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len: Int
        while (true) {
            len = inputStream.read(buffer)
            if (len == -1)
                break
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    private fun getImageInputStream(bitmapImage: Bitmap): InputStream {
        val bytes = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }

    private fun createImageValues(fileName: String): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.PNG")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1)   // 파일 쓰기 중일 때 다른 접근 Pending
        }
        return values
    }
}