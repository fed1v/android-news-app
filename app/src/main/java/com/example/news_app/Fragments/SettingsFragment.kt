package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import com.example.news_app.LoginActivity
import com.example.news_app.R
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {
    private lateinit var current_view: View
    private lateinit var button_logout: Button
    private lateinit var text_user_name: TextView
    private lateinit var text_user_email: TextView

    private var googleSignInAccount: GoogleSignInAccount? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view = LayoutInflater.from(context).inflate(R.layout.fragment_settings, container, false)
        text_user_email = current_view.findViewById(R.id.text_user_email)
        text_user_name = current_view.findViewById(R.id.text_user_name)

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        if(googleSignInAccount != null){
            text_user_email.text = googleSignInAccount!!.email
            text_user_name.text = googleSignInAccount!!.displayName
        } else{
            text_user_email.text = Firebase.auth.currentUser?.email?:"-"
            text_user_name.text = Firebase.auth.currentUser?.displayName?:"-"
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        button_logout = current_view.findViewById(R.id.btn_logout)
        button_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            googleSignInClient.signOut()
            Firebase.auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }


        requireActivity().onBackPressedDispatcher.addCallback{
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        return current_view
    }
}