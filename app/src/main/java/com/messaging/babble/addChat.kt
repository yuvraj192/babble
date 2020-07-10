package com.messaging.babble

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class addChat : AppCompatActivity() {
    var tv_phonebook: TextView? = null
    var arrayList: ArrayList<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        if (conView != null) {
            val webSettings = conView!!.settings
            conView.settings.javaScriptEnabled = true
            conView!!.webViewClient = WebViewClient()
            conView!!.webChromeClient = WebChromeClient()



            conView!!.loadUrl("file:///android_asset/addChat.html")
            conView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }






        arrayList = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            getcontact()
        }
    }

        private fun getcontact() {
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
                arrayList!!.add(
                    """
    $name
    $mobile
    """.trimIndent()
                )
                tv_phonebook!!.text = arrayList.toString()
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            if (requestCode == 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getcontact()
                }
            }
        }
    }
