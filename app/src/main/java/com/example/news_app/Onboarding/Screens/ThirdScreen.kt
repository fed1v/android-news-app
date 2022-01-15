package com.example.news_app.Onboarding.Screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.news_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ThirdScreen : Fragment() {
    private lateinit var v: View
    private lateinit var viewPager: ViewPager2
    private lateinit var finish: TextView
    private lateinit var btn_category: Button

    private val categories = arrayOf("Any", "Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology")
    private var category_num = 2


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
        v = inflater.inflate(R.layout.fragment_third_screen, container, false)

        viewPager = requireActivity().findViewById(R.id.viewPager)
        finish = v.findViewById(R.id.finish)
        btn_category = v.findViewById(R.id.btn_category)

        finish.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_mainActivity)
            onBoardingFinished()
        }

        initDatabase()

        btn_category.setOnClickListener {
            openCategorySettings()
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

    private fun onBoardingFinished(){
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

    private fun openCategorySettings() {
        var category_n = category_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default category")
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
        val userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val editor = userPreferences?.edit()
        editor?.putString("User category", category)
        editor?.apply()
    }

}