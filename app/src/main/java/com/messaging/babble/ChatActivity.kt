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
    private val socket: Socket = IO.socket("http://192.168.31.183:4600/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

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
                fun send(data: String, to: String, time: String) {
                    socket.emit("message", data)
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

                }
            }

            socket.on("message", Emitter.Listener {
                Toast.makeText(applicationContext, "New Mesg", Toast.LENGTH_SHORT).show()
            })

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

}
