package com.messaging.babble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

class HomeActivity : AppCompatActivity() {
    private val socket: Socket = IO.socket("http://iotine.zapto.org:4600/")
    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val channelId = "com.messaging.notiex"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        socket.connect()
        socket.emit("join", "yuvj")


        if (homeView != null){
            val websettings = homeView!!.settings
            homeView.settings.javaScriptEnabled = true
            homeView!!.webViewClient = WebViewClient()
            homeView!!.webChromeClient = WebChromeClient()

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun load() {
                    openActivity()
                }
            }, "chat")

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun logout(){
                    logAct()
                    finish()
                }
            }, "app")

            homeView.addJavascriptInterface(object: Any(){
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

            homeView.loadUrl("file:///android_asset/home.html")
            homeView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                }
            }

            socket.on("message", Emitter.Listener { args ->
                notifyMessage(args[0].toString(), args[1].toString(), args[2].toString())
            })

        }

        }
    private fun notifyMessage(msg: String, by: String, time: String){
        sendnoti(msg)
    }
    private fun openActivity(){
        val intent = Intent(this@HomeActivity, ChatActivity::class.java)
        startActivity(intent)
    }
    private fun logAct(){
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        startActivity(intent)

    }

    private fun sendnoti(msg: String){
        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,"test",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this,channelId)
                .setSmallIcon(R.mipmap.logo_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.logo))
                .setContentIntent(pendingIntent)
        }else{

            builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.logo))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234,builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}