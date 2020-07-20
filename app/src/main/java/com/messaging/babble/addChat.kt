package com.messaging.babble

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class addChat : AppCompatActivity() {
    var arrayList: ArrayList<String>? = null

    var phoneNumber: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        }

        if (conView != null) {
            val webSettings = conView!!.settings
            conView.settings.javaScriptEnabled = true
            conView!!.webViewClient = WebViewClient()
            conView!!.webChromeClient = WebChromeClient()

            conView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close(){
                    finish()
                }
            }, "contacts")

            conView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun load(num: String, name: String) {
                    openActivity(num, name)
                    finish()
                }
            }, "chat")

            conView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun reload(){
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
            }, "cons")

            conView.addJavascriptInterface(object: Any(){
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

            conView!!.loadUrl("file:///android_asset/addChat.html")
            conView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    conView.loadUrl("javascript:updateNumber($phoneNumber)")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
                    } else {
                        getcontact()
                    }
                }
            }
        }
        arrayList = ArrayList()

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

                  conView.loadUrl("javascript:addContactToList('$name', '$mobile')")
            }
        }

    private fun openActivity(num: String, name: String){
        val intent = Intent(this@addChat, ChatActivity::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        intent.putExtra("toNum", num)
        intent.putExtra("toName", name)
        startActivity(intent)
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
