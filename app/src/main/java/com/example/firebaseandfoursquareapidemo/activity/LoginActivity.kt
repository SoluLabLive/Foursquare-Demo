package com.example.firebaseandfoursquareapidemo.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.firebaseandfoursquareapidemo.R
import com.example.firebaseandfoursquareapidemo.model.User
import com.example.firebaseandfoursquareapidemo.utils.Utils
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity() {

    lateinit var callbackManager: CallbackManager
    private val TAG = LoginActivity::class.java.simpleName

    private var loginType = ""
    private lateinit var database: DatabaseReference
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        context = this
//        printKeyHash()
        init()

        btnFBLogin.setOnClickListener {
            if (Utils.isConnectedToInternet(this))
                onFacebookClick()
            else
                Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun init() {
        FirebaseApp.initializeApp(this)
        callbackManager = CallbackManager.Factory.create()
        database = FirebaseDatabase.getInstance().reference
    }

    private fun onFacebookClick() {


        progressBar.visibility = View.VISIBLE
        LoginManager.getInstance().logOut()
        callFacebookSdk()
        loginType = "facebook"
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList<String>("public_profile", "email"/*, "user_location"*/))

    }


    private fun callFacebookSdk() {


        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {


            override fun onSuccess(loginResult: LoginResult) {
                // App code

                val accessToken = loginResult.accessToken
                val request = GraphRequest.newMeRequest(accessToken) { json, response ->
                    // MyApplication code
                    //                        Utils.progressDialog(mContext);
                    try {
                        if (response.error != null) {
                            // handle error
                            println("ERROR")
                        } else {
                            println("Success")
                            try {

                                getData(json)

                            } catch (e: Exception) {
                                e.printStackTrace()
                                progressBar.visibility = View.GONE
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        progressBar.visibility = View.GONE
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,first_name,last_name,gender")
                request.parameters = parameters
                request.executeAsync()

            }

            override fun onCancel() {
                // App code
                progressBar.visibility = View.GONE
                println("LoginActivity.onCancel")
            }

            override fun onError(exception: FacebookException) {
                // App code
                println("LoginActivity.onError : ")
                exception.printStackTrace()
                progressBar.visibility = View.GONE
            }


        })
    }

    private fun getData(json: JSONObject) {
        progressBar.visibility = View.GONE
        val jsonresult = json.toString()
        System.out.println("JSON Result--> $jsonresult");
        var FBuserId = ""
        var FBuserFirstName = ""
        var FBuserLastName = ""
        var FBuserEmail = ""

        if (json.has("email"))
            FBuserEmail = json.getString("email")
        if (json.has("first_name"))
            FBuserFirstName = json.getString("first_name")
        if (json.has("last_name"))
            FBuserLastName = json.getString("last_name")
        if (json.has("id"))
            FBuserId = json.getString("id")
        if (FBuserEmail.isEmpty()) {

            Toast.makeText(context, "Email id not public", Toast.LENGTH_LONG).show()

        } else {

            Toast.makeText(context, "Welcome $FBuserFirstName", Toast.LENGTH_LONG).show()

            if (Utils.isConnectedToInternet(this))
                writeNewUser(FBuserId, FBuserFirstName, FBuserLastName, FBuserEmail)
            else
                Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()

        }

    }

    private fun writeNewUser(userId: String, firstName: String, lastName: String, email: String?) {
        val user = User(userId, firstName, lastName, email)
        database.child("users").child(userId).setValue(user).addOnSuccessListener {
            val intent = Intent(context, MapMain::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }

    private fun printKeyHash() {
        try {
            val info = packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("KeyHash mkm", "Exception(NoSuchAlgorithmException) : $e")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {

            if (loginType.equals("facebook", ignoreCase = true)) {
                try {
                    callbackManager.onActivityResult(requestCode, resultCode, data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}
