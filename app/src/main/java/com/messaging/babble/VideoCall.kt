package com.messaging.babble

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_video_call.*


class VideoCall : AppCompatActivity() {

    var rid: String? = null
    var to: String? = null
    private val RECORD_REQUEST_CODE = 101
    private val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        val intent = intent
        rid = intent.getStringExtra("roomId")
        to = intent.getStringExtra("callTo")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        }

        val permission: Int

        permission = ContextCompat.checkSelfPermission(
            this@VideoCall,
            Manifest.permission.RECORD_AUDIO
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@VideoCall,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                // Show an expanation to the user *asynchronously* -- don't block
            } else {
                ActivityCompat.requestPermissions(
                    this@VideoCall,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            }
        }

        if(vcView!= null){
            val webSettings = vcView!!.settings
            vcView.settings.javaScriptEnabled = true
            vcView.settings.setUserAgentString(DESKTOP_USER_AGENT);
            vcView.settings.setMediaPlaybackRequiresUserGesture(false);
            vcView!!.webViewClient=  WebViewClient()
            vcView!!.webChromeClient=  WebChromeClient()

            if(rid == "CALLINGFROMHERE"){
                vcView!!.loadUrl("https://iotine.zapto.org:4600/")
            }else {
                vcView!!.loadUrl("https://iotine.zapto.org:4600/$rid")
            }

            vcView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close() {
                    finish()
                }
            }, "vc")

            vcView.setWebChromeClient(object : WebChromeClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }
            })
            vcView!!.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                    super.onPageStarted(view,url,favicon)
                    requestPermission()

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if(rid == "CALLINGFROMHERE"){
                        vcView!!.loadUrl("javascript:CallTo('$to')")
                    }else {
                        vcView!!.loadUrl("javascript:justJoin()")
                    }
                }
            }
        }
    }

    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||  ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }
}