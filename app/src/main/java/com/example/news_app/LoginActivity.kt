package com.example.news_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKAuthResult
import com.vk.api.sdk.auth.VKScope
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    companion object {
        const val RC_SIGN_IN: Int = 123
    }

    private lateinit var buttonGoogle: ImageButton
    private lateinit var buttonVK: ImageButton
    private lateinit var login_button_facebook: LoginButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var googleSignInAccount: GoogleSignInAccount? = null
    private lateinit var callbackManager: CallbackManager

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference

    private var signWay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(!InternetConnection.isConnected()){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        auth = FirebaseAuth.getInstance()
        createRequest()

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (/*googleSignInAccount != null || */auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        initView()

    }

    override fun onBackPressed() {
        finishAffinity()
        finish()
    }

    private fun initView() {

        buttonGoogle = findViewById(R.id.btn_google)
        buttonVK = findViewById(R.id.btn_vk)

        login_button_facebook = findViewById(R.id.login_button_facebook)
        login_button_facebook.setPermissions("email", "public_profile")
        callbackManager = CallbackManager.Factory.create()
        login_button_facebook.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result!!.accessToken)
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException?) {
                    println("Facebook Error: $error")
                }
            })

        buttonGoogle.setOnClickListener {
            signWay = "Google"
            signInWithGoogle()
        }

        buttonVK.setOnClickListener {
            signWay = "VK"
            signInWithVK()
        }
    }

    private fun signInWithVK() {
        VK.login(this, arrayListOf(VKScope.EMAIL))
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("64993158520-mg5ljp4isqf7b4ak2cthrbmo9ncrtdeh.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // ?
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK/*|| signWay == "Google"*/) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
            return
        } else if (signWay == "VK") {
            val callback = object : VKAuthCallback {
                override fun onLogin(token: VKAccessToken) {
                    val email = token.email!!
                    val password = "vk-" + token.userId     // TODO ??
                    auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val isNewUser = task.result.signInMethods?.isEmpty() ?: false

                                if (isNewUser) {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { taskk ->
                                            if (taskk.isSuccessful) {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Create successful",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                initUserInDatabase()
                                                startOnBoardingActivity()
                                            } else {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Create failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { taskk ->
                                            if (taskk.isSuccessful) {
                                                initUserInDatabase()
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Sign in successful",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                println("Current user: ${auth.currentUser}")
                                                startMainActivity()
                                            } else {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Sign in failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }


                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Auth failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }


                }

                override fun onLoginFailed(errorCode: Int) {
                    Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
            if(!VK.onActivityResult(requestCode, resultCode, data, callback)){
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
        } else{
            println("Facebook")
            try {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }



        if (data == null) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    initUserInDatabase()

                    userStatsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isFirstLogin = !snapshot.hasChild("total time")
                            println("isFirstLogin: $isFirstLogin")

                            if (isFirstLogin) {
                                startOnBoardingActivity()
                            } else {
                                startMainActivity()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })

                } else {
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initUserInDatabase() {
        val user = auth.currentUser
        println("User: $user")
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user.uid)
            val profile = GoogleSignIn.getLastSignedInAccount(this)
            val userMap = mapOf(
                "email" to (user.email ?: profile?.email),
                "name" to (user.displayName ?: profile?.displayName),
            )
            println(userMap)
            Log.d("sout", userMap.toString())
            currentUserReference.updateChildren(userMap)
            userBookmarksReference = currentUserReference.child("bookmarks")
            userStatsReference = currentUserReference.child("stats")
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    initUserInDatabase()

                    userStatsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isFirstLogin = !snapshot.hasChild("total time")
                            println("isFirstLogin: $isFirstLogin")

                            if (isFirstLogin) {
                                startOnBoardingActivity()
                            } else {
                                startMainActivity()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startOnBoardingActivity() {
        val intent = Intent(applicationContext, OnBoardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}