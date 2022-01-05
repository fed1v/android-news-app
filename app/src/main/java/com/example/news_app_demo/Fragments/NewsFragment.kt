package com.example.news_app_demo.Fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.*
import com.example.news_app_demo.Models.NewsApiResponse
import com.example.news_app_demo.Models.NewsHeadlines
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class NewsFragment : Fragment(), SelectListener, View.OnClickListener {

    val names: Array<String> = arrayOf("Ivan", "Oleg", "Sergey")

    //    private var manager: RequestManager = RequestManager(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
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

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var current_view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view = LayoutInflater.from(context).inflate(R.layout.fragment_news, container, false)

        current_category = null

        searchView = current_view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                dialog.setTitle("Fetching news articles of $query")
                dialog.show()

                val manager: RequestManager = RequestManager(requireContext())

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

        dialog = ProgressDialog(requireContext())
        dialog.setTitle("Fetching news articles...")
        dialog.show()

        btn_all = current_view.findViewById(R.id.btn_all)
        btn_all.setOnClickListener(this)

        btn_business = current_view.findViewById(R.id.btn_business)
        btn_business.setOnClickListener(this)

        btn_entertainment = current_view.findViewById(R.id.btn_entertainment)
        btn_entertainment.setOnClickListener(this)

        btn_general = current_view.findViewById(R.id.btn_general)
        btn_general.setOnClickListener(this)

        btn_health = current_view.findViewById(R.id.btn_health)
        btn_health.setOnClickListener(this)

        btn_science = current_view.findViewById(R.id.btn_science)
        btn_science.setOnClickListener(this)

        btn_sports = current_view.findViewById(R.id.btn_sports)
        btn_sports.setOnClickListener(this)

        btn_technology = current_view.findViewById(R.id.btn_technology)
        btn_technology.setOnClickListener(this)

        btn_options = current_view.findViewById(R.id.btn_options)
        btn_options.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            openCountrySettings()
                        }
                        1 -> {
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

        val manager = RequestManager(requireContext())
        manager.getNewsHeadlines(listener, "general", null, string_sources, current_country_api)



        //

        return current_view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {  // Bookmarks
                println("Bookmarks option")
                return true
            }
            122 -> {  // Share
                shareLink(item.groupId)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun shareLink(item: Int) {
        var shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, adapter.headlines[item].url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }

    private fun openCountrySettings() {
        var country_n = country_num
        MaterialAlertDialogBuilder(requireContext())
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
        val temp_checled_sources = current_checked_sources
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Source")
            .setMultiChoiceItems(sources, current_checked_sources) { dialog, which, isChecked ->
                temp_checled_sources[which] = isChecked
            }
            .setPositiveButton("Ok") { dialog, which ->
                current_checked_sources = temp_checled_sources
                changeSources()
                println("Ok $which")
            }
            .setNeutralButton("Reset") { dialog, which ->
                string_sources = null
                for (i in current_checked_sources.indices) current_checked_sources[i] = false
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
        string_sources = TextUtils.join(",", result_sources_list)
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

        val manager: RequestManager = RequestManager(requireContext())
        if (current_category == null) {
            manager.getNewsEverything(listener, null, "CNN,techcrunch")
        } else {
            manager.getNewsHeadlines(listener, category, null, string_sources, current_country_api)
        }
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        startActivity(
            Intent(requireContext(), DetailsActivity::class.java)
                .putExtra("data", headlines)
        )
    }

    private val listener: OnFetchDataListener<NewsApiResponse> =
        object : OnFetchDataListener<NewsApiResponse> {
            override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
                if (newsHeadlinesList.isEmpty()) {
                    Toast.makeText(requireContext(), "Nothing found", Toast.LENGTH_SHORT).show()
                } else {
                    showNews(newsHeadlinesList)
                }
                dialog.dismiss()
            }

            override fun onError(message: String) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
        }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        recyclerView = current_view.findViewById(R.id.news_recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        adapter = NewsAdapter(requireContext(), newsHeadlinesList, this)
        recyclerView.adapter = adapter
    }


}