package com.messaging.babble

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*

class ProfilePage : AppCompatActivity() {

    var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                            requestPermissions(permissions, ProfilePage.PERMISSION_CODE);

                        } else {
                            pickImageFromGallery();
                        }

                    }
                    else{
                        pickImageFromGallery()
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
                    profileView.loadUrl("javascript:setPno('$phoneNumber')")
                }
            }


        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent, IMAGE_PICK_CODE)

    }

    companion object {
        private val IMAGE_PICK_CODE = 1000;
        private val PERMISSION_CODE = 1001;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
                else{
                    Toast.makeText(this, "Permission Denied ", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            //image_view.setImageURI(data?.data)
            var imurl = data?.data
            profileView.post(Runnable {
                profileView.loadUrl("javascript:prevImage('$imurl', $phoneNumber)")
            })
        }
    }
}