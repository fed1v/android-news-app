package com.example.news_app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.news_app.Fragments.*
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.NewsHeadlines
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

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

    private val CHANNEL_ID = "NewsApp"
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private var userCountry = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initDatabase()

        createNotificationChannel()

        println(user)
        if(user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }


        saveUserSettingsToSharedPreferences()


        if(showHeadlines){
            openFragment(NewsFragment())
        } else{
            openFragment(NewsEverythingFragment())
        }
    }

    private fun saveUserSettingsToSharedPreferences() {
        val pref = getSharedPreferences("User settings", MODE_PRIVATE)
        val editor = pref.edit()
        currentUserReference.child("country").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val country = snapshot.getValue(String::class.java)
                editor.putString("User country", country)
                editor.apply()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        currentUserReference.child("language").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val language = snapshot.getValue(String::class.java)
                editor.putString("User language", language)
                editor.apply()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        currentUserReference.child("category").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val category = snapshot.getValue(String::class.java)
                editor.putString("User category", category)
                editor.apply()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        if(user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if(InternetConnection.isConnected()){
            timeStart = System.currentTimeMillis()
            userStatsReference.child("total time").get().addOnCompleteListener {
                totalTimeInDatabase = it.result.getValue(Long::class.java)
            }
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
        if (user != null) {
            val userTime = timeEnd - timeStart
            var totalTime = userTime
            if (totalTimeInDatabase != null) {
                totalTime += totalTimeInDatabase!!
            }
            userStatsReference.updateChildren(mapOf("total time" to totalTime))
        }
    }

    private fun initView(){
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NewsAppNotifications"
            val descriptionText = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}