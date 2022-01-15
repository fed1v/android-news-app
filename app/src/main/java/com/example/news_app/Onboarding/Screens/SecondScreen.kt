package com.example.news_app.Onboarding.Screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.news_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SecondScreen : Fragment() {
    private lateinit var v: View
    private lateinit var viewPager: ViewPager2
    private lateinit var next: TextView

    private lateinit var btn_language: Button

    private val languagesMap = mapOf(
        "Any" to null,
        "Arabic" to "ar",
        "Chinese" to "zh",
        "Dutch" to "nl",
        "English" to "en",
        "French" to "fr",
        "German" to "de",
        "Hebrew" to "he",
        "Italian" to "it",
        "Norwegian" to "no",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Sami" to "se",
        "Spanish" to "es",
    )
    private var language_num = 0

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_second_screen, container, false)

        next = v.findViewById(R.id.next)
        btn_language = v.findViewById(R.id.btn_language)
        viewPager = requireActivity().findViewById(R.id.viewPager)

        initDatabase()

        next.setOnClickListener {
            viewPager.currentItem = 2
        }

        btn_language.setOnClickListener {
            openLanguageSettings()
        }


        return v
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
        }
    }

    private fun openLanguageSettings() {
        var language_n = language_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default language")
            .setSingleChoiceItems(languagesMap.keys.toTypedArray(), language_num) { dialog, which ->
                language_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                language_num = language_n
                changeLanguage()
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun changeLanguage() {
        val language = languagesMap.keys.toTypedArray()[language_num]
        val languageMap = mapOf("language" to language)
        currentUserReference.updateChildren(languageMap)
        val userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val editor = userPreferences?.edit()
        editor?.putString("User language", language)
        editor?.apply()
    }

}