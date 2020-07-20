package com.messaging.babble

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(loginView!= null){
            val webSettings = loginView!!.settings
            loginView.settings.javaScriptEnabled = true
            loginView!!.webViewClient=  WebViewClient()
            loginView!!.webChromeClient=  WebChromeClient()

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun performClick(phoneNumber: String) {
                    goHome(phoneNumber)
                    finish()
                }
            }, "valid")

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun backColor() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val window = window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.statusBarColor = Color.WHITE
                    }
                }
            }, "change")



            loginView!!.loadUrl("file:///android_asset/login.html")
            loginView!!.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                    super.onPageStarted(view,url,favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }
    }
private fun goHome(phoneNumber: String){
    val intent = Intent(this@MainActivity, HomeActivity::class.java)
    intent.putExtra("phoneNumber", phoneNumber)
    startActivity(intent)
}
}

