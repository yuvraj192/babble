package com.messaging.babble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

    var list:ListView = TODO()

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        list = (ListView)

    }
}