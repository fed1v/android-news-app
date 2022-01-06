package com.example.news_app_demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.news_app_demo.Fragments.BookmarksFragment
import com.example.news_app_demo.Fragments.NewsFragment
import com.example.news_app_demo.Fragments.SettingsFragment
import com.example.news_app_demo.Fragments.StatsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openFragment(NewsFragment())

        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener {item ->
            val selectedFragment: Fragment = when(item.itemId){
                R.id.bookmarksFragment -> BookmarksFragment()
                R.id.statsFragment -> StatsFragment()
                R.id.settingsFragment -> SettingsFragment()
                else -> NewsFragment()
            }
            openFragment(selectedFragment)

            return@setOnItemSelectedListener true
        }


    }

    fun openFragment(selectedFragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }
}