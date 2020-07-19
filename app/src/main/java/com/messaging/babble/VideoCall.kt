package com.messaging.babble

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_video_call.*

class VideoCall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        if(vcView!= null){
            val webSettings = vcView!!.settings
            vcView.settings.javaScriptEnabled = true
            vcView!!.webViewClient=  WebViewClient()
            vcView!!.webChromeClient=  WebChromeClient()

            vcView!!.loadUrl("file:///android_asset/videocall.html")
            vcView!!.webViewClient = object : WebViewClient(){
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