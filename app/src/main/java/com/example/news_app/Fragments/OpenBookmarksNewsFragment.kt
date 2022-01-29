package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.news_app.InternetConnection
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.NewsHeadlinesStats
import com.example.news_app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.nio.charset.Charset

class OpenBookmarksNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var v: View
    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private lateinit var current_category: String
    private lateinit var urlHashCode: String

    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var timeInDatabase: Long? = 0

    private var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context)
            .inflate(R.layout.fragment_open_bookmarks_news, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return v
        }

        timeStart = System.currentTimeMillis()
        urlHashCode = Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()

        if (headlines.category == null || headlines.category == "") {
            current_category = "other"
        } else {
            current_category = headlines.category!!
        }

        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)

        initDatabase()
        getPasswordFromDatabase()

        userStatsReference.child(current_category).child(urlHashCode).child("time").get()
            .addOnCompleteListener {
                timeInDatabase = it.result.getValue(Long::class.java)
            }

        webView = v.findViewById(R.id.web_view_open_bookmarks_news)
        webView.webViewClient = WebViewClient()
        webView.loadUrl(headlines.url)

        toolbar = v.findViewById(R.id.toolbar_open_bookmarks_news)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mi_share -> shareLink()
                R.id.mi_delete -> deleteFromBookmarks()
                R.id.mi_notes -> {
                    if (password == null) {
                        openNotes()
                    } else {
                        openSecurityDialog()
                    }
                }
            }
            true
        }


        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.bookmarksFragment
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, BookmarksFragment())
                .commit()
        }

        return v
    }

    private fun openSecurityDialog() {
        val dialog = NotesSecurityDialogFragment(headlines)
        try {
            dialog.show(requireActivity().supportFragmentManager, "Notes Security")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPasswordFromDatabase() {
        currentUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                password = snapshot.child("notesPassword").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            timeEnd = System.currentTimeMillis()
            addNewsToStats()
        }
        super.onDestroy()
    }

    private fun addNewsToStats() {
        val userTime = timeEnd - timeStart
        var time = userTime
        if (timeInDatabase != null) {
            time += timeInDatabase!!
        }
        val newsStats = NewsHeadlinesStats(headlines, time)
        userStatsReference.child(current_category).child(urlHashCode).setValue(newsStats)
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

    private fun openNotes() {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_container, NotesFragment(headlines))
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun deleteFromBookmarks() {
        val urlHashCode =
            Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(urlHashCode).removeValue()
        Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

    private fun shareLink() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, headlines.url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }
}