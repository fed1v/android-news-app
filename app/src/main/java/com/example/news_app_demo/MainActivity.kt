package com.example.news_app_demo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsApiResponse
import com.example.news_app_demo.Models.NewsHeadlines

class MainActivity : AppCompatActivity(), SelectListener, View.OnClickListener {
    //    private var manager: RequestManager = RequestManager(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private lateinit var dialog: ProgressDialog

    private lateinit var b1: Button
    private lateinit var b2: Button
    private lateinit var b3: Button
    private lateinit var b4: Button
    private lateinit var b5: Button
    private lateinit var b6: Button
    private lateinit var b7: Button

    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                dialog.setTitle("Fetching news articles of $query")
                dialog.show()

                val manager: RequestManager = RequestManager(this@MainActivity)
                manager.getNewsHeadlines(listener, "general", query)

                return true
            }

            override fun onQueryTextChange(q: String?): Boolean {
                return false
            }
        })

        dialog = ProgressDialog(this)
        dialog.setTitle("Fetching news articles...")
        dialog.show()

        b1 = findViewById(R.id.btn_1)
        b1.setOnClickListener(this)

        b2 = findViewById(R.id.btn_2)
        b2.setOnClickListener(this)

        b3 = findViewById(R.id.btn_3)
        b3.setOnClickListener(this)

        b4 = findViewById(R.id.btn_4)
        b4.setOnClickListener(this)

        b5 = findViewById(R.id.btn_5)
        b5.setOnClickListener(this)

        b6 = findViewById(R.id.btn_6)
        b6.setOnClickListener(this)

        b7 = findViewById(R.id.btn_7)
        b7.setOnClickListener(this)


        val manager: RequestManager = RequestManager(this)
        manager.getNewsHeadlines(listener, "general", null)
    }

    override fun onClick(v: View?) {
        val button: Button = v as Button
        val category: String = button.text.toString()

        dialog.setTitle("Fetching news articles of $category")
        dialog.show()

        val manager: RequestManager = RequestManager(this)
        manager.getNewsHeadlines(listener, category, null)
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        startActivity(
            Intent(this, DetailsActivity::class.java)
                .putExtra("data", headlines)
        )

    }

    private val listener: OnFetchDataListener<NewsApiResponse> =
        object : OnFetchDataListener<NewsApiResponse> {
            override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
                if (newsHeadlinesList.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Nothing found", Toast.LENGTH_SHORT).show()
                } else {
                    showNews(newsHeadlinesList)
                }

                dialog.dismiss()

            }

            override fun onError(message: String) {
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }

        }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        recyclerView = findViewById(R.id.recycler_main)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        adapter = CustomAdapter(this, newsHeadlinesList, this)
        recyclerView.adapter = adapter
    }


}