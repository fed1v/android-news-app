package com.example.news_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.news_app.Bookmarks.BookmarksFragment
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.InternetConnection
import com.example.news_app.News.NewsFragment
import com.example.news_app.Stats.StatsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var databaseHelper: DatabaseHelper
    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var totalTimeInDatabase: Long? = 0
    private val CHANNEL_ID = "NewsApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        databaseHelper = DatabaseHelper(this)

        if (databaseHelper.user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        createNotificationChannel()
        saveUserSettingsToSharedPreferences()
    }

    private fun saveUserSettingsToSharedPreferences() {
        val pref = getSharedPreferences("User settings", MODE_PRIVATE)
        val editor = pref.edit()
        databaseHelper.currentUserReference.child("country")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val country = snapshot.getValue(String::class.java)
                    editor.putString("User country", country)
                    editor.apply()
                    openFragment(NewsFragment())
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        databaseHelper.currentUserReference.child("language")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val language = snapshot.getValue(String::class.java)
                    editor.putString("User language", language)
                    editor.apply()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        databaseHelper.currentUserReference.child("category")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val category = snapshot.getValue(String::class.java)
                    editor.putString("User category", category)
                    editor.apply()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        if (databaseHelper.user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if (InternetConnection.isConnected()) {
            timeStart = System.currentTimeMillis()
            databaseHelper.userStatsReference.child("total time").get().addOnCompleteListener {
                totalTimeInDatabase = it.result.getValue(Long::class.java)
            }
        }
        super.onResume()
    }

    private fun openFragment(selectedFragment: Fragment) {
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
        if (databaseHelper.user != null) {
            val userTime = timeEnd - timeStart
            var totalTime = userTime
            if (totalTimeInDatabase != null) {
                totalTime += totalTimeInDatabase!!
            }
            databaseHelper.userStatsReference.updateChildren(mapOf("total time" to totalTime))
        }
    }

    private fun initView() {
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.bookmarksFragment -> BookmarksFragment()
                R.id.statsFragment -> StatsFragment()
                R.id.settingsFragment -> SettingsFragment()
                else -> NewsFragment()
            }
            openFragment(selectedFragment)
            true
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NewsAppNotifications"
            val descriptionText = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}