package com.example.news_app.News

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.EncryptionHelper
import com.example.news_app.Helpers.InternetConnection
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.NewsHeadlinesStats
import com.example.news_app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class OpenNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var v: View
    private lateinit var newsWebView: WebView
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var current_category: String
    private lateinit var urlHashCode: String

    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var timeInDatabase: Long? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.fragment_open_news, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }

        timeStart = System.currentTimeMillis()
        databaseHelper = DatabaseHelper(requireContext(), headlines)
        initView()

        if (headlines.category == null || headlines.category == "") {
            current_category = "other"
        } else {
            current_category = headlines.category!!
        }

        urlHashCode = EncryptionHelper.getSHA1(headlines.url)
        getTime()
        return v
    }

    private fun getTime() {
        databaseHelper.userStatsReference.child(current_category).child(urlHashCode).child("time")
            .get().addOnCompleteListener {
                timeInDatabase = it.result.getValue(Long::class.java)
            }
    }

    private fun initView() {
        newsWebView = v.findViewById(R.id.web_view_news)
        newsWebView.webViewClient = WebViewClient()
        newsWebView.loadUrl(headlines.url)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)

        toolbar = v.findViewById(R.id.toolbar_open_news)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mi_share -> shareLink()
                R.id.mi_bookmark -> addToBookmarks()
            }
            return@setOnMenuItemClickListener true
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.newsFragment
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }
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
        databaseHelper.userStatsReference.child(current_category).child(urlHashCode)
            .setValue(newsStats)
    }

    private fun addToBookmarks() {
        databaseHelper.addToBookmarks(headlines)
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