package ru.startandroid.develop.handleradvmessage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import java.util.Random

const val STATUS_NONE = 0
const val STATUS_CONNECTING = 1
const val STATUS_CONNECTED = 2
const val STATUS_DOWNLOAD_START = 3
const val STATUS_DOWNLOAD_FILE = 4
const val STATUS_DOWNLOAD_END = 5
const val STATUS_DOWNLOAD_NONE = 6

class MainActivity : AppCompatActivity() {

    var h: Handler? = null

    var tvStatus: TextView? = null
    var pbDownload: ProgressBar? = null
    var btnConnect: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById<View>(R.id.tvStatus) as TextView
        pbDownload = findViewById<View>(R.id.pbDownload) as ProgressBar
        btnConnect = findViewById<View>(R.id.btnConnect) as Button

        h = object : Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what) {
                    STATUS_NONE -> {
                        btnConnect!!.isEnabled = true
                        pbDownload!!.visibility = View.GONE
                    }
                    STATUS_CONNECTING -> {
                        btnConnect!!.isEnabled = false
                        tvStatus!!.text = "Connecting"
                    }
                    STATUS_CONNECTED -> {
                        tvStatus!!.text = "Connected"
                    }
                    STATUS_DOWNLOAD_START -> {
                        tvStatus!!.text = "Start download ${msg.arg1} files"
                        pbDownload!!.max = msg.arg1
                        pbDownload!!.progress = 0
                        pbDownload!!.visibility = View.VISIBLE
                    }
                    STATUS_DOWNLOAD_FILE -> {
                        tvStatus!!.text = "Downloading. Left ${msg.arg2} files"
                        pbDownload!!.progress = msg.arg1
                        saveFile(msg.obj as ByteArray)
                    }
                    STATUS_DOWNLOAD_END -> {
                        tvStatus!!.text = "Download complete!"
                    }
                    STATUS_DOWNLOAD_NONE -> {
                        tvStatus!!.text = "No files for download"
                    }
                }
                h!!.sendEmptyMessage(STATUS_NONE)
            }
        }
    }
    fun onClick(v: View?) {
        val t = Thread(object : Runnable {
            var msg: Message? = null
            var file: ByteArray? = null
            var rand: Random? = Random()
            override fun run() {
                try {
                    h!!.sendEmptyMessage(STATUS_CONNECTING)
                    Thread.sleep(1000)

                    h!!.sendEmptyMessage(STATUS_CONNECTED)

                    Thread.sleep(1000)
                    val filesCount: Int = rand!!.nextInt(5)

                    if (filesCount == 0) {
                        h!!.sendEmptyMessage(STATUS_DOWNLOAD_NONE)
                        Thread.sleep(1500)
                        h!!.sendEmptyMessage(STATUS_NONE)
                        return
                    }
                    msg = h!!.obtainMessage(STATUS_DOWNLOAD_START, filesCount, 0)

                    h!!.sendMessage(msg!!)

                    for (i in 1..filesCount) {
                        file = downloadFile()
                        msg = h!!.obtainMessage(STATUS_DOWNLOAD_FILE, i, filesCount - i, file)
                        h!!.sendMessage(msg!!)
                    }
                    h!!.sendEmptyMessage(STATUS_DOWNLOAD_END)

                    Thread.sleep(1500)
                    h!!.sendEmptyMessage(STATUS_NONE)
                } catch (e:InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
        t.start()
    }
    fun downloadFile() : ByteArray {
        Thread.sleep(2000)
        return ByteArray(1024)
    }

    fun saveFile(file: ByteArray?) {}
}