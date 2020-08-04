package com.messaging.babble

import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {

        Log.d("TAG", "Refreshed : $token")
        val settings = getSharedPreferences("btor56ungh", 0)
        val editor = settings.edit()
        editor.putString("buir4554", token.toString())
        editor.commit()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val messageRecieved: String? = remoteMessage.data["message"]
        if (messageRecieved != null) {
            Log.d("TAG", messageRecieved)
        }
    }
}