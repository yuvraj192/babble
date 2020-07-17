package com.messaging.babble

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if(profileView != null){
            val webSettings = profileView!!.settings
            profileView.settings.javaScriptEnabled = true
            profileView!!.webViewClient= WebViewClient()
            profileView!!.webChromeClient = WebChromeClient()

            profileView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close(){
                    finish()
                }
            }, "profile")


            profileView!!.loadUrl("file:///android_asset/profile.html")
            profileView!!.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                    super.onPageStarted(view,url,favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }


        }
    }
}