package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.NewsHeadlinesStats
import com.example.news_app.R
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset

class OpenNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var v: View
    private lateinit var newsWebView: WebView
    private lateinit var toolbar: Toolbar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private lateinit var urlHashCode: String

    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var timeInDatabase: Long? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        timeStart = System.currentTimeMillis()
        initDatabase()

        urlHashCode = Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()

        userStatsReference.child(urlHashCode).child("time").get().addOnCompleteListener {
            timeInDatabase = it.result.getValue(Long::class.java)
        }

        v = inflater.inflate(R.layout.fragment_open_news, container, false)
        newsWebView = v.findViewById(R.id.web_view_news)
        newsWebView.webViewClient = WebViewClient()
        newsWebView.loadUrl(headlines.url)

        toolbar = v.findViewById(R.id.toolbar_open_news)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mi_share -> shareLink()
                R.id.mi_bookmark -> addToBookmarks()
            }
            return@setOnMenuItemClickListener true
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        return v
    }

    override fun onDestroy() {
        timeEnd = System.currentTimeMillis()
        addNewsToStats()
        super.onDestroy()
    }

    private fun addNewsToStats() {
        val userTime = timeEnd - timeStart
        var time = userTime
        if (timeInDatabase != null) {
            time += timeInDatabase!!
        }
        val newsStats = NewsHeadlinesStats(headlines, time)
        userStatsReference.child(urlHashCode).setValue(newsStats)
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

    private fun addToBookmarks() {
        val urlHashCode =
            Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(urlHashCode).setValue(headlines)
        Toast.makeText(requireContext(), "Bookmark added", Toast.LENGTH_SHORT).show()
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