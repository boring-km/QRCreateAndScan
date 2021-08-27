package com.boring.qrcreateandscan

import android.app.Application
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class QRApp : Application() {

    private val TAG = "QRApp"

    /**
     * Application 이 시작되기 전 MyApp 의 onCreate 실행
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, " *** onCreate()")
        val date = Date(System.currentTimeMillis())
        val format = SimpleDateFormat("yyyyMMddhhmmss")
        val time: String = format.format(date)
        if (isExternalStorageWritable()) {
            //read, write 둘다 가능
            val appDirectory = File(Environment.getExternalStorageDirectory().toString() + "/" + TAG)
            val logDirectory = File(appDirectory.toString() + "/logs")
            val logFile = File(logDirectory, "logcat_$time.txt")
            Log.d(TAG, "*** onCreate() - appDirectory :: " + appDirectory.absolutePath)
            Log.d(TAG, "*** onCreate() - logDirectory :: " + logDirectory.absolutePath)
            Log.d(TAG, "*** onCreate() - logFile :: $logFile")

            //appDirectory 폴더 없을 시 생성
            if (!appDirectory.exists()) {
                appDirectory.mkdirs()
            }

            //logDirectory 폴더 없을 시 생성
            if (!logDirectory.exists()) {
                logDirectory.mkdirs()
            }

            //이전 logcat 을 지우고 파일에 새 로그을 씀
            try {
                var process = Runtime.getRuntime().exec("logcat -c")
                process = Runtime.getRuntime().exec("logcat -f $logFile")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (isExternalStorageReadable()) {
            //read 만 가능
        } else {
            //접근 불가능
        }
    }


    /**
     * 외부저장소 read/write 가능 여부 확인
     * @return
     */
    private fun isExternalStorageWritable(): Boolean {
        val state: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED.equals(state)
    }


    /**
     * 외부저장소 read 가능 여부 확인
     * @return
     */
    fun isExternalStorageReadable(): Boolean {
        val state: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
    }

}