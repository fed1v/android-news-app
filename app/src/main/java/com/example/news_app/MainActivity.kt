package com.example.news_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.news_app.Fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    var showHeadlines: Boolean = true //TODO

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var totalTimeInDatabase: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDatabase()



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

    override fun onResume() {
        timeStart = System.currentTimeMillis()
        userStatsReference.child("total time").get().addOnCompleteListener {
            totalTimeInDatabase = it.result.getValue(Long::class.java)
        }
        super.onResume()
    }

    private fun openFragment(selectedFragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }

    override fun onPause() {
        timeEnd = System.currentTimeMillis()
        addTimeToStats()
        super.onPause()
    }

    private fun addTimeToStats() {
        val userTime = timeEnd - timeStart
        var totalTime = userTime
        if(totalTimeInDatabase != null){
            totalTime += totalTimeInDatabase!!
        }
        userStatsReference.updateChildren(mapOf("total time" to totalTime))
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
}