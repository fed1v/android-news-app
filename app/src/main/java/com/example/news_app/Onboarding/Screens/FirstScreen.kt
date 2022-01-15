package com.example.news_app.Onboarding.Screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.news_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_second_screen.view.*

class FirstScreen : Fragment() {
    private lateinit var v: View
    private lateinit var next: TextView
    private lateinit var viewPager: ViewPager2

    private  lateinit var btn_country: Button

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_first_screen, container, false)
        next = v.findViewById(R.id.next)
        btn_country = v.findViewById(R.id.btn_country)

        viewPager = requireActivity().findViewById(R.id.viewPager)

        initDatabase()

        next.setOnClickListener {
            viewPager.currentItem = 1
        }

        btn_country.setOnClickListener {
            openCountrySettings()
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

    private fun openCountrySettings() {
        var country_n = country_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default country")
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
        val userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val editor = userPreferences?.edit()
        editor?.putString("User country", country)
        editor?.apply()
    }
}