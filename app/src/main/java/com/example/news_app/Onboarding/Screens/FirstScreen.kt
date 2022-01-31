package com.example.news_app.Onboarding.Screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.NewsOptionsHelper
import com.example.news_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FirstScreen : Fragment() {
    private lateinit var v: View
    private lateinit var next: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var btn_country: Button

    private lateinit var databaseHelper: DatabaseHelper

    private val countriesMap = NewsOptionsHelper.countries
    private var country_num = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_first_screen, container, false)
        databaseHelper = DatabaseHelper(requireContext())
        initView()
        return v
    }

    private fun initView() {
        next = v.findViewById(R.id.next)
        btn_country = v.findViewById(R.id.btn_country)
        viewPager = requireActivity().findViewById(R.id.viewPager)

        next.setOnClickListener {
            viewPager.currentItem = 1
        }
        btn_country.setOnClickListener {
            openCountrySettings()
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
        databaseHelper.currentUserReference.updateChildren(countryMap)
        val userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val editor = userPreferences?.edit()
        editor?.putString("User country", country)
        editor?.apply()
    }
}