package com.messaging.babble

import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONException
import org.json.JSONObject


class ChatActivity : AppCompatActivity() {
    private val socket: Socket = IO.socket("http://iotine.zapto.org:4600/")
    var phoneNumber: String? = null
    var toNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")
        toNum = intent.getStringExtra("toNum")

        if (chatView != null){
            val websettings = chatView!!.settings
            chatView.settings.javaScriptEnabled = true
            chatView!!.webViewClient = WebViewClient()
            chatView!!.webChromeClient = WebChromeClient()

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close() {
                    finish()
                }
            }, "chat")

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun send(data: String, to: String, from: String, time: String) {
                    socket.emit("message", data, to, from, time)
                }
            }, "msg")

            chatView.addJavascriptInterface(object: Any(){
                @JavascriptInterface
                fun vibrate(){
                    val vibrator = applicationContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(30)
                    }
                }

            }, "device")


            chatView.loadUrl("file:///android_asset/chat.html")

            chatView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    chatView.loadUrl("javascript:updateNumber($phoneNumber, $toNum)")
                }
            }

            socket.on("message", Emitter.Listener { args ->
                addMessage(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString())
            })

        }

    }

    private fun addMessage(msg: String, by: String, from: String, time: String){
        chatView.post(Runnable {
            chatView.loadUrl("javascript:recieveMessage('$msg', '$time')")
        })
    }

}
