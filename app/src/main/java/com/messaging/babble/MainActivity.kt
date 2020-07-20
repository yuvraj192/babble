package com.messaging.babble

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import java.util.Base64


class MainActivity : AppCompatActivity() {
    lateinit var phonenum: String
    private var mVerificationId: String? = null
    //firebase auth object
    private var mAuth: FirebaseAuth? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings = getSharedPreferences("btor56ungh", 0)
        mAuth = FirebaseAuth.getInstance();

        if(loginView!= null){
            val webSettings = loginView!!.settings
            loginView.settings.javaScriptEnabled = true
            loginView!!.webViewClient=  WebViewClient()
            loginView!!.webChromeClient=  WebChromeClient()

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun performClick(phoneNumber: String) {
                    val settings = getSharedPreferences("btor56ungh", 0)
                    val editor = settings.edit()
                    editor.putString("ughn8dh", phoneNumber.toString())
                    editor.commit()
                    finish()
                }
            }, "valid")

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun home(phoneNumber: String) {
                   goHome(phoneNumber)
                }
            }, "go")

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun OTP() {
                    sendVerificationCode(phonenum)
                }
            }, "get")

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun toast(msg: String) {
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                }
            }, "make")

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun save(phoneNumber: String) {
                    phonenum = phoneNumber
                }
            }, "reg")

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

            loginView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun performClick(code: String) {
                    verifyVerificationCode(code);
                }
            }, "verify");

            loginView!!.loadUrl("file:///android_asset/login.html")
            loginView!!.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?){
                    super.onPageStarted(view,url,favicon)

                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if(settings.getString("ughn8dh", "").toString() != ""){
                        var pn: String = settings.getString("ughn8dh", "").toString()
                        loginView.loadUrl("javascript:goTohome('$pn')")
                    }
                }
            }
        }
    }

    private fun sendVerificationCode(mobile: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91$mobile",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallbacks
        )
    }

    private val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    loginView.loadUrl("javascript:putOTP('$code')")
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)

                mVerificationId = s
                loginView.loadUrl("javascript:showOTPBox()")
            }
        }


    private fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this@MainActivity,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        loginView.loadUrl("javascript:finishSetup()")
                    } else {
                        var message =
                            "Somthing is wrong, we will fix it soon..."
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered..."
                            Toast.makeText(this@MainActivity , message, Toast.LENGTH_LONG).show()
                        }

                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                })
    }


private fun goHome(phoneNumber: String){
    val intent = Intent(this@MainActivity, HomeActivity::class.java)
    intent.putExtra("phoneNumber", phoneNumber)
    startActivity(intent)
    finish()
}
}

