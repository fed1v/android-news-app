package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.news_app.LoginActivity
import com.example.news_app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {
    private lateinit var current_view: View
    private lateinit var button_logout: Button
    private lateinit var text_user_name: TextView
    private lateinit var text_user_email: TextView

    private var signInAccount: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view = LayoutInflater.from(context).inflate(R.layout.fragment_settings, container, false)
        text_user_email = current_view.findViewById(R.id.text_user_email)
        text_user_name = current_view.findViewById(R.id.text_user_name)

        signInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())

        text_user_email.text = signInAccount?.email?:"nullEmail"
        text_user_name.text = signInAccount?.displayName?:"nullName"

        button_logout = current_view.findViewById(R.id.btn_logout)
        button_logout.setOnClickListener {
            Toast.makeText(context, "Logout...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        return current_view
    }

}