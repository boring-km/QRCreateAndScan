package com.boring.qrcreateandscan.bitmap

import android.content.ContentResolver
import android.graphics.Bitmap

class NoneQRCodeImage : BitmapImage() {
    override fun saveImage(
        image: Bitmap,
        contentResolver: ContentResolver
    ) {

    }
}