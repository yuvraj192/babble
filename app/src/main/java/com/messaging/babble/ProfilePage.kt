package com.messaging.babble

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.gallery_item.*

class ProfilePage : AppCompatActivity() {

    var phoneNumber: String? = null
    var galleryAdapter: GalleryAdapter? = null

    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.BLACK
        }

        if(profileView != null){
            val websettings = profileView!!.settings
            profileView.settings.javaScriptEnabled = true

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            {

                websettings.setAllowFileAccess(true);
                websettings.setAllowContentAccess(true);
                websettings.setAllowFileAccessFromFileURLs(true);
                websettings.setAllowUniversalAccessFromFileURLs(true);

            }


            profileView!!.webViewClient= WebViewClient()
            profileView!!.webChromeClient = WebChromeClient()

            profileView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close(){
                    finish()
                }
            }, "profile")

            profileView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun Gallery() {
                    /*Toast.makeText(applicationContext, "HERE", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@ChatActivity, GalleryOpen::class.java)
                    startActivity(intent)*/
                    if (ContextCompat.checkSelfPermission(
                            this@ProfilePage,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@ProfilePage,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Gallery.MY_READ_PERMISSION_CODE
                        )
                    } else {
                        loadImages()
                    }
                }
            }, "photo")


            profileView!!.loadUrl("file:///android_asset/profile.html")
            profileView!!.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                    super.onPageStarted(view,url,favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    profileView.loadUrl("javascript:setPno('$phoneNumber');")
                }
            }


        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun loadImages() {
        val images: List<String> = ImagesGallery.listOfImages(this)
        galleryAdapter = GalleryAdapter(this, images, object : GalleryAdapter.PhotoListener {
            override fun onPhotoClick(path: String?) {
                Toast.makeText(this@ProfilePage, "" + path, Toast.LENGTH_LONG).show()
            }
        })
        var totalImages = images.size
        profileView.post(Runnable {
            val handler = Handler()

            for(image in images){
                handler.postDelayed({
                    profileView.loadUrl("javascript:prevImage('$image', $phoneNumber, '$totalImages')")
                }, 2000)
            }
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_READ_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read External Storage Permission Granted", Toast.LENGTH_SHORT)
                    .show()
                loadImages()
            } else {
                Toast.makeText(this, "Read External Storage Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val MY_READ_PERMISSION_CODE = 101
    }
}