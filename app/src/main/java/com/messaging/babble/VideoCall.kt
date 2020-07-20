package com.messaging.babble

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_video_call.*


class VideoCall : AppCompatActivity() {

    var phoneNumber: String? = null
    var toNum: String? = null
    var toName: String? = null
    var ucid: String? = null
    private val RECORD_REQUEST_CODE = 101
    private val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")
        toNum = intent.getStringExtra("to")
        toName = intent.getStringExtra("name")
        ucid = intent.getStringExtra("ucid")

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

            vcView!!.loadUrl("https://appr.tc/r/babble-video-room--vc-UCID_$ucid")
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