package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.NewsAdapter
import com.example.news_app.R
import com.example.news_app.SelectListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.nio.charset.Charset


class BookmarksFragment : Fragment(), SelectListener {
    private lateinit var current_view: View
    private lateinit var adapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference

    private lateinit var bookmarks: ArrayList<NewsHeadlines>

    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view =
            LayoutInflater.from(context).inflate(R.layout.fragment_bookmarks, container, false)

        bookmarks = arrayListOf()
        initDatabase()
        initView()

        userBookmarksReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookmarks.removeAll { true }
                for (dataSnapshot in snapshot.children) {
                    val headline = dataSnapshot.getValue(NewsHeadlines::class.java)
                    bookmarks.add(headline!!)
                }
                showNews(bookmarks)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        requireActivity().onBackPressedDispatcher.addCallback{
            bottomNavigationView.selectedItemId = R.id.newsFragment
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        return current_view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            121 -> {
                deleteFromBookmarks(item.groupId)  // TODO remove
                return true
            }
            122 ->{
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
        userBookmarksReference.child(urlHashCode).removeValue()
        Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
        }
    }

    private fun initView() {
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)
        recyclerView = current_view.findViewById(R.id.bookmarks_recyclerView)
        recyclerView.setHasFixedSize(true)
    }

    private fun showNews(news: List<NewsHeadlines>) {
        if(context != null){
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