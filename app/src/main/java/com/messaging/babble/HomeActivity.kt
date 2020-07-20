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
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_add_chat.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private val socket: Socket = IO.socket("http://iotine.zapto.org:4600/")
    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val CHANNEL_ID = "com.messaging.babble"

    var phoneNumber: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")

        socket.connect()
        socket.emit("join", phoneNumber)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        }

        if (homeView != null){
            val websettings = homeView!!.settings
            homeView.settings.javaScriptEnabled = true
            homeView!!.webViewClient = WebViewClient()
            homeView!!.webChromeClient = WebChromeClient()

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun load(num: String, name: String) {
                    openActivity(num, name)
                    //sendNoti("9891192693", "Mummy 😘😘", "Hello 😀😀")
                }
            }, "chat")

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun add() {
                    addActivity()
                }
            }, "newChat")

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun logout(){
                    logAct()
                    socket.disconnect()
                    val settings = getSharedPreferences("btor56ungh", 0)
                    val editor = settings.edit()
                    editor.putString("ughn8dh", "")
                    editor.commit()
                    finish()
                }
            }, "app")

            homeView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun getName(num: String) {
                    getContactName(num)
                }
            }, "contacts")

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

            homeView.addJavascriptInterface(object: Any(){
                @JavascriptInterface
                fun load(){
                    val intent = Intent(this@HomeActivity, ProfilePage::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }

            }, "profile")

            homeView.loadUrl("file:///android_asset/home.html")
            homeView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    homeView.loadUrl("javascript:updateNumber($phoneNumber)")
                }
            }

            socket.on("message", Emitter.Listener { args ->
                notifyMessage(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString())
            })

        }

        }
    private fun notifyMessage(msg: String, to: String, from: String ,time: String){
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val mobile =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            if(from == mobile){
                sendNoti(from, name, msg)
                homeView.post(Runnable {
                    homeView.loadUrl("javascript:updateChatList('$from', '$name', '$msg')")
                })
            }
        }
    }
    private fun openActivity(num: String, name: String){
        val intent = Intent(this@HomeActivity, ChatActivity::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        intent.putExtra("toNum", num)
        intent.putExtra("toName", name)
        startActivity(intent)
    }

    private fun getContactName(num: String) {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val mobile =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            if(num == mobile){
                homeView.post(Runnable {
                    homeView.loadUrl("javascript:updateName('$name', '$num')")
                })
            }
        }
    }

    private fun addActivity(){
        val intent = Intent(this@HomeActivity, addChat::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        startActivity(intent)
    }

    private fun logAct(){
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        startActivity(intent)

    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Notification"
            val descriptionText = "This is your notification description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNoti(num: String, name: String, msg: String){
        createNotificationChannel()
        val notificationLayout = RemoteViews(packageName, R.layout.custom_notif)

        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("phoneNumber", phoneNumber)
        intent.putExtra("toNum", num)
        intent.putExtra("toName", name)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.logo)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationLayout.setTextViewText(R.id.title, name)
        notificationLayout.setTextViewText(R.id.msg, msg)

        with(NotificationManagerCompat.from(this)){
            notify(0, builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}