package com.example.news_app_demo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsApiResponse
import com.example.news_app_demo.Models.NewsHeadlines

class MainActivity : AppCompatActivity(), SelectListener {
    private var manager: RequestManager = RequestManager(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private lateinit var dialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialog = ProgressDialog(this)
        dialog.setTitle("Fetching news articles...")
        dialog.show()

        manager.getNewsHeadlines(listener, "general", null)
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        startActivity(Intent(this, DetailsActivity::class.java)
            .putExtra("data", headlines))

    }

    private val listener: OnFetchDataListener<NewsApiResponse> = object: OnFetchDataListener<NewsApiResponse>{
        override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
            showNews(newsHeadlinesList)
            dialog.dismiss()
        }

        override fun onError(message: String) {}

    }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        recyclerView = findViewById(R.id.recycler_main)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        adapter = CustomAdapter(this, newsHeadlinesList, this)
        recyclerView.adapter = adapter
    }


}