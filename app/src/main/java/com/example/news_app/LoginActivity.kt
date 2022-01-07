package com.example.news_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    companion object {
        const val RC_SIGN_IN: Int = 123
    }

    private lateinit var buttonToMainActivity: Button
    private lateinit var buttonFacebook: ImageButton
    private lateinit var buttonGoogle: ImageButton
    private lateinit var buttonTwitter: ImageButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()


        val user: FirebaseUser? = auth.currentUser

        println("-----------------------OnStart; user: ${user.toString()}----------------------------")

        if(user != null){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        println("--------------------------OnCreate---------------------------------")

        auth = FirebaseAuth.getInstance()

        createRequest()

        buttonToMainActivity = findViewById(R.id.button_to_mainActivity)
        buttonToMainActivity.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        buttonFacebook = findViewById(R.id.btn_facebook)
        buttonGoogle = findViewById(R.id.btn_google)
        buttonTwitter = findViewById(R.id.btn_twitter)

        buttonFacebook.setOnClickListener{
            println("Facebook")
        }

        buttonGoogle.setOnClickListener{
            signIn()
        }

        buttonTwitter.setOnClickListener{
            println("Twitter")
        }

    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                println("----------------------------Account: ${account.toString()}---------------------------")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.status}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("----------------------User(firebaseAuthWithGoogle): ${user?.email?:"user-is-null"}------------------------")
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT)
                }
            }
    }


}