package com.example.news_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.news_app.Fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    var showHeadlines: Boolean = true //TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if(user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        if(showHeadlines){
            openFragment(NewsFragment())
        } else{
            openFragment(NewsEverythingFragment())
        }

        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener {item ->
            val selectedFragment: Fragment = when(item.itemId){
                R.id.bookmarksFragment -> BookmarksFragment()
                R.id.statsFragment -> StatsFragment()
                R.id.settingsFragment -> SettingsFragment()
                else -> if(showHeadlines) NewsFragment() else NewsEverythingFragment()
            }
            openFragment(selectedFragment)
            true
        }
    }

    fun openFragment(selectedFragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }
}