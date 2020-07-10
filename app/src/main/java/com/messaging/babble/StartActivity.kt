package com.messaging.babble

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        if (startView != null) {
            val websettings = startView!!.settings
            startView.settings.javaScriptEnabled = true
            startView!!.webViewClient = WebViewClient()
            startView!!.webChromeClient = WebChromeClient()

            startView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun loadfeat() {
                    newActivity()
                }
            }, "start")

        }



    startView.loadUrl("file:///android_asset/feat1.html")
    startView!!.webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

        }
    }

}

    private fun newActivity(){
        val intent = Intent(this@StartActivity, MainActivity::class.java)
        startActivity(intent)
    }

}
