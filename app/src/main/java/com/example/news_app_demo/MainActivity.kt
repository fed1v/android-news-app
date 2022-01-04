package com.example.news_app_demo

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.TextUtils.join
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsApiResponse
import com.example.news_app_demo.Models.NewsHeadlines
import com.example.news_app_demo.Models.Source
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), SelectListener, View.OnClickListener {
    //    private var manager: RequestManager = RequestManager(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private lateinit var dialog: ProgressDialog

    private val options: Array<String> = arrayOf("Country", "Source", "Everything/Headlines")
    private val countries: Array<String> = arrayOf("USA", "Russia", "Ukraine", "France")
    private val countries_api: Array<String> = arrayOf("us", "ru", "ua", "fr")

    private var country_num: Int = 0

    private var current_category: String? = null
    private var current_sources: String? = "CNN, techcrunch"
    private var current_country: String? = countries[country_num]
    private var current_country_api: String? = countries_api[country_num]


    private var sources: Array<String> = arrayOf("CNN", "TechCrunch", "ABC News")
    private var sources_api: Array<String> = arrayOf("cnn", "techcrunch", "abc-news")
    private var current_checked_sources: BooleanArray = booleanArrayOf(false, true, true)
    private var string_sources: String? = null

    private lateinit var btn_all: Button
    private lateinit var btn_business: Button
    private lateinit var btn_entertainment: Button
    private lateinit var btn_general: Button
    private lateinit var btn_health: Button
    private lateinit var btn_science: Button
    private lateinit var btn_sports: Button
    private lateinit var btn_technology: Button

    private lateinit var btn_options: ImageButton

    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        current_category = null

        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                dialog.setTitle("Fetching news articles of $query")
                dialog.show()

                val manager: RequestManager = RequestManager(this@MainActivity)

                if (current_category == null) {
                    manager.getNewsEverything(listener, query, current_sources)
                } else {
                    manager.getNewsHeadlines(
                        listener,
                        current_category,
                        query,
                        string_sources,
                        current_country_api
                    )
                }

                return true
            }

            override fun onQueryTextChange(q: String?): Boolean {
                return false
            }
        })

        dialog = ProgressDialog(this)
        dialog.setTitle("Fetching news articles...")
        dialog.show()

        btn_all = findViewById(R.id.btn_all)
        btn_all.setOnClickListener(this)

        btn_business = findViewById(R.id.btn_business)
        btn_business.setOnClickListener(this)

        btn_entertainment = findViewById(R.id.btn_entertainment)
        btn_entertainment.setOnClickListener(this)

        btn_general = findViewById(R.id.btn_general)
        btn_general.setOnClickListener(this)

        btn_health = findViewById(R.id.btn_health)
        btn_health.setOnClickListener(this)

        btn_science = findViewById(R.id.btn_science)
        btn_science.setOnClickListener(this)

        btn_sports = findViewById(R.id.btn_sports)
        btn_sports.setOnClickListener(this)

        btn_technology = findViewById(R.id.btn_technology)
        btn_technology.setOnClickListener(this)

        btn_options = findViewById(R.id.btn_options)
        btn_options.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            println("Country: $current_country")
                            openCountrySettings()
                        }
                        1 -> {
                            println("Source")
                            openSourceSettings()
                        }
                        2 -> {
                            println("Everything/Headlines")
                        }
                    }
                }
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .show() //TODO
        }

        val manager = RequestManager(this)
        manager.getNewsHeadlines(listener, "general", null, string_sources, current_country_api)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            121 -> {
                println("Bookmarks option")
                return true
            }
            122 -> {
                println("Share option")
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun openCountrySettings() {
        var country_n = country_num
        MaterialAlertDialogBuilder(this)
            .setTitle("Country")
            .setSingleChoiceItems(countries, country_num) { dialog, which ->
                println("index: $which")
                country_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                country_num = country_n
                changeCurrentCountry()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                current_country = countries[country_num]
            }
            .show()
    }

    fun openSourceSettings() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Source")
            .setMultiChoiceItems(sources, current_checked_sources) { dialog, which, isChecked ->
                current_checked_sources[which] = isChecked
            }
            .setPositiveButton("Ok") { dialog, which ->
                changeSources()
                println("Ok $which")
            }
            .setNeutralButton("Reset") { dialog, which ->
                println("Reset $which")

            }
            .setNegativeButton("Cancel") { dialog, which ->
                println("Cancel $which")

            }
            .show() //TODO
    }

    fun changeCurrentCountry() {
        current_country = countries[country_num]
        current_country_api = countries_api[country_num]
    }

    fun changeSources() {
        val result_sources_list = arrayListOf<String>()
        for (i in 0..2) {
            if (current_checked_sources[i]) {
                result_sources_list.add(sources_api[i])
            }
        }
        string_sources = join(",", result_sources_list)
        println("String sources: $string_sources")
    }

    override fun onClick(v: View?) {
        val button: Button = v as Button
        var category: String? = null
        if (button.text.toString() != "all") {
            category = button.text.toString()
        }
        current_category = category

        dialog.setTitle("Fetching news articles of $category")
        dialog.show()

        val manager: RequestManager = RequestManager(this)
        if (current_category == null) {
            manager.getNewsEverything(listener, null, "CNN,techcrunch")
        } else {
            manager.getNewsHeadlines(listener, category, null, string_sources, current_country_api)
        }
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