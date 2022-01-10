package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.NewsHeadlinesStats
import com.example.news_app.NewsInStatsAdapter
import com.example.news_app.R
import com.example.news_app.SelectInStatsListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.apache.commons.lang3.time.DurationFormatUtils
import java.nio.charset.Charset

class StatsFragment : Fragment(), SelectInStatsListener {
    private lateinit var v: View
    private lateinit var adapter: NewsInStatsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView_total_time: TextView

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference

    private lateinit var stats: ArrayList<NewsHeadlinesStats>
    private var totalTime: Long = 0
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_stats, container, false)

        stats = arrayListOf()
        initDatabase()
        initView()


        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.newsFragment
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        userStatsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stats.removeAll { true }
                for (dataSnapshot in snapshot.children) {
                    if (dataSnapshot.value is Long) {
                        totalTime = dataSnapshot.getValue(Long::class.java) ?: 0
                    } else{
                        val headline = dataSnapshot.getValue(NewsHeadlinesStats::class.java)
                        stats.add(headline!!)
                    }
                }
                showNews(stats, totalTime)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return v
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                deleteFromBookmarks(item.groupId)
                return true
            }
            122 -> {
                shareLink(item.groupId)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun shareLink(item: Int) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, adapter.headlines[item].url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }

    private fun deleteFromBookmarks(item: Int) {
        val headline = adapter.headlines[item]
        val urlHashCode =
            Hashing.sha1().hashString(headline.url, Charset.defaultCharset()).toString()
        userStatsReference.child(urlHashCode).removeValue()
        Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userStatsReference = currentUserReference.child("stats")
        }
    }

    private fun initView() {
        textView_total_time = v.findViewById(R.id.textView_total_time)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)
        recyclerView = v.findViewById(R.id.stats_recyclerView)
        recyclerView.setHasFixedSize(true)
    }

    private fun showNews(news: List<NewsHeadlinesStats>, totalTime: Long) {
        if (context != null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = NewsInStatsAdapter(requireContext(), news, this)
            recyclerView.adapter = adapter
            val string_total_time: String =
                DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss", true)
            val time = "Total time: $string_total_time"
            textView_total_time.text = time
        }
    }

    override fun onNewsClicked(headlines: NewsHeadlinesStats) {
        val headlns = NewsHeadlines(
            source = headlines.source,
            author = headlines.author,
            title = headlines.title,
            description = headlines.description,
            url = headlines.url,
            urlToImage = headlines.urlToImage,
            publishedAt = headlines.publishedAt,
            content = headlines.content ?: ""
        )
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, OpenNewsFragment(headlns)) // TODO
            .commit()
    }
}