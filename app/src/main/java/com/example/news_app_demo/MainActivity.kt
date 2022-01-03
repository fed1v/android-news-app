package com.example.news_app_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsApiResponse
import com.example.news_app_demo.Models.NewsHeadlines

class MainActivity : AppCompatActivity() {
    private var manager: RequestManager = RequestManager(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager.getNewsHeadlines(listener, "general", null)
    }

    private val listener: OnFetchDataListener<NewsApiResponse> = object: OnFetchDataListener<NewsApiResponse>{
        override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
            showNews(newsHeadlinesList)
        }

        override fun onError(message: String) {
            TODO("Not yet implemented")
        }

    }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        recyclerView = findViewById(R.id.recycler_main)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        adapter = CustomAdapter(this, newsHeadlinesList)
        recyclerView.adapter = adapter
    }


}