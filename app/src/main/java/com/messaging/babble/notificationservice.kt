package com.messaging.babble

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

class notificationservice : Service() {
    private val socket: Socket = IO.socket("https://iotine.zapto.org:4600/")
    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val CHANNEL_ID = "com.messaging.babble"

    var phoneNumber: String? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val settings = getSharedPreferences("btor56ungh", 0)

        if(settings.getString("ughn8dh", "").toString() != "") {
            var pn: String = settings.getString("ughn8dh", "").toString()

            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = intent
            phoneNumber = intent.getStringExtra("phoneNumber")

            socket.connect()
            socket.emit("join", phoneNumber)

            socket.on("message", Emitter.Listener { args ->
                notifyMessage(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString(), args[4].toString())
            })

            socket.on("incoming-vc", Emitter.Listener { args ->
                    //openVC(args[0].toString())
            })

        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun notifyMessage(msg: String, to: String, from: String ,time: String, mid: String){
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
            }
        }
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
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        notificationLayout.setTextViewText(R.id.title, name)
        notificationLayout.setTextViewText(R.id.msg, msg)

        with(NotificationManagerCompat.from(this)){
            notify(0, builder.build())
        }
    }

    private fun openVC(rid: String){
        val intent = Intent(applicationContext, VideoCall::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("roomId", rid)
        startActivity(intent)
    }
}