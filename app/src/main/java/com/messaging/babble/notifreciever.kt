package com.messaging.babble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class notifreciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val notifier = Intent(context, notificationservice::class.java)

        if (Intent.ACTION_LOCKED_BOOT_COMPLETED == intent.action) {
            if (extras != null) {
                notifier.putExtra("phoneNumber", extras.getString("phoneNumber"))
            }
            context.startService(notifier)
        }

        if (extras != null) {
            notifier.putExtra("phoneNumber", extras.getString("phoneNumber"))
        }
        context.startService(notifier)
    }
}