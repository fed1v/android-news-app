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
import com.example.news_app.DatabaseHelper
import com.example.news_app.NewsOptionsHelper
import com.example.news_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SecondScreen : Fragment() {
    private lateinit var v: View
    private lateinit var viewPager: ViewPager2
    private lateinit var next: TextView
    private lateinit var btn_language: Button

    private val languagesMap = NewsOptionsHelper.languages
    private var language_num = 0

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_second_screen, container, false)
        databaseHelper = DatabaseHelper(requireContext())
        initView()
        return v
    }

    private fun initView() {
        next = v.findViewById(R.id.next)
        btn_language = v.findViewById(R.id.btn_language)
        viewPager = requireActivity().findViewById(R.id.viewPager)

        next.setOnClickListener {
            viewPager.currentItem = 2
        }
        btn_language.setOnClickListener {
            openLanguageSettings()
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
        databaseHelper.currentUserReference.updateChildren(languageMap)
        val userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val editor = userPreferences?.edit()
        editor?.putString("User language", language)
        editor?.apply()
    }
}