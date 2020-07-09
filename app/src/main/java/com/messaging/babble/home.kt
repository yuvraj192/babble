package com.messaging.babble

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

class home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if(homeView!=null){
            val webSettings = loginView!!.settings
            homeView.settings.javaScriptEnabled = true
            homeView!!.webViewClient=  WebViewClient()
            homeView!!.webChromeClient=  WebChromeClient()
        }
        homeView!!.loadUrl("file:///android_asset/home.html")
        homeView!!.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                super.onPageStarted(view,url,favicon)

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }
}