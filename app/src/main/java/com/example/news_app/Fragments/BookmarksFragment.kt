package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Models.NewsHeadlines
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BookmarksFragment : Fragment(), SelectListener {
    private lateinit var v: View
    private lateinit var adapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var bookmarks: ArrayList<NewsHeadlines>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_bookmarks, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return v
        }

        bookmarks = arrayListOf()
        initView()
        databaseHelper = DatabaseHelper(requireContext())

        databaseHelper.userBookmarksReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookmarks.removeAll { true }
                for (dataSnapshot in snapshot.children) {
                    val headline = dataSnapshot.getValue(NewsHeadlines::class.java)
                    bookmarks.add(headline!!)
                }
                showNews(bookmarks)
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
        databaseHelper.deleteFromBookmarks(headline)
    }

    private fun initView() {
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)
        recyclerView = v.findViewById(R.id.bookmarks_recyclerView)
        recyclerView.setHasFixedSize(true)

        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.newsFragment
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, NewsFragment())
                ?.commit()
        }
    }

    private fun showNews(news: List<NewsHeadlines>) {
        if (context != null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = NewsAdapter(requireContext(), news, this)
            recyclerView.adapter = adapter
        }
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, OpenBookmarksNewsFragment(headlines))
            .commit()
    }
}