package com.messaging.babble

import android.Manifest
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*


class ChatActivity : AppCompatActivity() {
    private val socket: Socket = IO.socket("http://iotine.zapto.org:4600/")
    var phoneNumber: String? = null
    var toNum: String? = null
    var toName: String? = null
    private val RECORD_REQUEST_CODE = 101
    private val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent
        phoneNumber = intent.getStringExtra("phoneNumber")
        toNum = intent.getStringExtra("toNum")
        toName = intent.getStringExtra("toName")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        }

        if (chatView != null){
            val websettings = chatView!!.settings
            chatView.settings.javaScriptEnabled = true

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            {

                websettings.setAllowFileAccess(true);
                websettings.setAllowContentAccess(true);
                websettings.setAllowFileAccessFromFileURLs(true);
                websettings.setAllowUniversalAccessFromFileURLs(true);

            }

            chatView.settings.setUserAgentString(DESKTOP_USER_AGENT);
            chatView.settings.setMediaPlaybackRequiresUserGesture(false);
            chatView!!.webViewClient = WebViewClient()
            chatView!!.webChromeClient = WebChromeClient()



            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun close() {
                    finish()
                }
            }, "chat")

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun Gallery() {
                    /*Toast.makeText(applicationContext, "HERE", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@ChatActivity, GalleryOpen::class.java)
                    startActivity(intent)*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                            requestPermissions(permissions, ChatActivity.PERMISSION_CODE);

                        } else {
                            pickImageFromGallery();
                        }

                    }
                    else{
                        pickImageFromGallery()
                    }
                }
            }, "photo")

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun send(data: String, to: String, from: String, time: String, mid: String) {
                    socket.emit("message", data, to, from, time, mid)
                }
            }, "msg")

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun call(ucid: String, to: String, from: String, name: String) {
                    signalUser(to, from, name, ucid)
                }
            }, "video")

            chatView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun delete(to: String, ucid: String, _id: String) {
                    socket.emit("deleteMsg", to, ucid, _id)

                }
            }, "msgg")

            chatView.addJavascriptInterface(object: Any(){
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

            chatView.addJavascriptInterface(object: Any(){
                @JavascriptInterface
                fun onMessage(mid: String, reaction: String, to: String, from: String){
                    socket.emit("reaction", mid, reaction, to.toString(), from.toString())
                }

            }, "react")

            chatView.addJavascriptInterface(object: Any(){
                @JavascriptInterface
                fun record(){
                    val permission: Int

                    permission = ContextCompat.checkSelfPermission(
                        this@ChatActivity,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permission != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@ChatActivity,
                                Manifest.permission.RECORD_AUDIO
                            )
                        ) {
                            // Show an expanation to the user *asynchronously* -- don't block
                        } else {
                            ActivityCompat.requestPermissions(
                                this@ChatActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                RECORD_REQUEST_CODE
                            )
                        }
                    }
                }

            }, "audio")



            chatView.loadUrl("file:///android_asset/chat.html")

            chatView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                        if (request.origin
                                .toString() == "file:///"
                        ) {
                            request.grant(request.resources)
                        } else {
                            request.deny()
                        }
                }
            }

            chatView!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    chatView.loadUrl("javascript:updateNumber($phoneNumber, $toNum, '$toName')")
                }
            }

            socket.on("message", Emitter.Listener { args ->
                addMessage(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString(), args[4].toString())
            })

            socket.on("deleteMsg", Emitter.Listener { args ->
                deletemsg(args[0].toString(), args[1].toString())
            })

            socket.on("reaction", Emitter.Listener { args ->
                reactOnMsg(args[0].toString(), args[1].toString())
            })

        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/* video/*")
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
            chatView.post(Runnable {
                chatView.loadUrl("javascript:prevImage('$imurl')")
            })
        }
    }

    private fun deletemsg(to: String, id: String){
        chatView.post(Runnable {
            var _id: String = id.toString()
            chatView.loadUrl("javascript:deleteMessage($to ,$_id)")
        })
    }

    private fun addMessage(msg: String, by: String, from: String, time: String, mid: String){
        chatView.post(Runnable {
            Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
            chatView.loadUrl("javascript:recieveMessage('$msg', '$time', '$mid', '$from')")
        })
    }

    private fun reactOnMsg(mid: String, reaction: String){
        chatView.post(Runnable {
            Toast.makeText(applicationContext, reaction, Toast.LENGTH_LONG)
            chatView.loadUrl("javascript:recieveReaction('$mid', '$reaction')")
        })
    }

    //video calls

    private fun signalUser(to: String, from: String, name: String, ucid: String){
        val intent = Intent(this@ChatActivity, VideoCall::class.java)
        intent.putExtra("phoneNumber", from)
        intent.putExtra("to", to)
        intent.putExtra("name", name)
        intent.putExtra("ucid", ucid)
        socket.emit("signal", to, from)
        startActivity(intent)
    }

}
