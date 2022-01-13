package com.example.news_app.Fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.news_app.LoginActivity
import com.example.news_app.R
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {
    private lateinit var v: View
    private lateinit var text_user_name: TextView
    private lateinit var text_user_email: TextView

    private lateinit var button_logout: Button
    private lateinit var button_language: Button
    private lateinit var button_country: Button
    private lateinit var button_category: Button

    private var googleSignInAccount: GoogleSignInAccount? = null
    private var googleSignInClient: GoogleSignInClient? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private val countriesMap = mapOf(  //TODO
        "Any" to null,
        "Argentina" to "ar",
        "Australia" to "au",
        "Austria" to "at",
        "Belgium" to "be",
        "Brazil" to "br",
        "Bulgaria" to "bg",
        "Canada" to "ca",
        "China" to "cn",
        "Colombia" to "co",
        "Cuba" to "cu",
        "Czechia" to "cz",
        "Egypt" to "eg",
        "France" to "fr",
        "Germany" to "de",
        "Greece" to "gr",
        "Honk Kong" to "hk",
        "Hungary" to "hu",
        "Indonesia" to "id",
        "Ireland" to "ie",
        "Israel" to "il",
        "India" to "in",
        "Italy" to "it",
        "Japan" to "jp",
        "Korea" to "kr",
        "Latvia" to "lv",
        "Lithuania" to "lt",
        "Morocco" to "ma",
        "Mexico" to "mx",
        "Malaysia" to "my",
        "Nigeria" to "ng",
        "Netherlands" to "nl",
        "Norway" to "no",
        "New Zealand" to "nz",
        "Philippines" to "ph",
        "Poland" to "pl",
        "Portugal" to "pt",
        "Romania" to "ro",
        "Russia" to "ru",
        "Serbia" to "rs",
        "Saudi Arabia" to "sa",
        "Sweden" to "se",
        "Singapore" to "sg",
        "Slovenia" to "si",
        "Slovakia" to "sk",
        "South Africa" to "za",
        "Switzerland" to "ch",
        "Thailand" to "th",
        "Turkey" to "tr",
        "Taiwan" to "tw",
        "Ukraine" to "ua",
        "United Arab Emirates" to "ae",
        "United Kingdom of Great Britain and Northern Ireland" to "gb",
        "USA" to "us",
        "Venezuela" to "ve"
    )
    private var country_num = 0

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

    private val categories = arrayOf("Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology")
    private var category_num = 2

    private lateinit var user_language: String
    private lateinit var user_country: String
    private lateinit var user_category: String

    private var userPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_settings, container, false)

        initDatabase()
        getUserSettings()
        initView()

        country_num = countriesMap.keys.indexOf(user_country)
        language_num = languagesMap.keys.indexOf(user_language)
        category_num = categories.indexOf(user_category)

        return v
    }

    private fun getUserSettings() {
        userPreferences = context?.getSharedPreferences("User settings", MODE_PRIVATE)
        user_country = userPreferences?.getString("User country", "us")?: "us"
        user_language = userPreferences?.getString("User language", "en")?: "en"
        user_category = userPreferences?.getString("User category", "general")?: "general"
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
            userStatsReference = currentUserReference.child("stats")
        }
    }

    private fun initView() {
        text_user_email = v.findViewById(R.id.text_user_email)
        text_user_name = v.findViewById(R.id.text_user_name)

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (googleSignInAccount != null) {
            text_user_email.text = googleSignInAccount!!.email
            text_user_name.text = googleSignInAccount!!.displayName
        } else {
            text_user_email.text = Firebase.auth.currentUser?.email ?: "-"
            text_user_name.text = Firebase.auth.currentUser?.displayName ?: "-"
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        button_logout = v.findViewById(R.id.btn_logout)
        button_category = v.findViewById(R.id.btn_category)
        button_language = v.findViewById(R.id.btn_language)
        button_country = v.findViewById(R.id.btn_country)

        button_logout.setOnClickListener {
            logout()
        }

        button_category.setOnClickListener {
            openCategorySettings()
        }

        button_language.setOnClickListener {
            openLanguageSettings()
        }

        button_country.setOnClickListener {
            openCountrySettings()
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }
    }

    private fun openCountrySettings() {
        println("Country settings")
        var country_n = country_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Country for sources")
            .setSingleChoiceItems(countriesMap.keys.toTypedArray(), country_num) { dialog, which ->
                country_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                country_num = country_n
                changeCountry()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeCountry() {
        val country = countriesMap.keys.toTypedArray()[country_num]
        val countryMap = mapOf("country" to country)
        currentUserReference.updateChildren(countryMap)
        editor = userPreferences?.edit()
        editor?.putString("User country", country)
        editor?.apply()
    }

    private fun openLanguageSettings() {
        var language_n = language_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Language for sources")
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
        editor = userPreferences?.edit()
        editor?.putString("User language", language)
        editor?.apply()
    }

    private fun openCategorySettings() {
        var category_n = category_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Category for sources")
            .setSingleChoiceItems(categories, category_num){ dialog, which ->
                category_n = which
            }
            .setPositiveButton("Ok"){ dialog, which ->
                category_num = category_n
                changeCategory()
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun changeCategory() {
        val category = categories[category_num]
        val categoryMap = mapOf("category" to category)
        currentUserReference.updateChildren(categoryMap)
        editor = userPreferences?.edit()
        editor?.putString("User category", category)
        editor?.apply()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        googleSignInClient?.signOut()
        Firebase.auth.signOut()
        val intent = Intent(context, LoginActivity::class.java)
        editor = userPreferences?.edit()
        editor?.clear()
        editor?.commit()
        startActivity(intent)
    }
}