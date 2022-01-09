package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.NewsHeadlines
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset


class NewsFragment : Fragment(), SelectListener, View.OnClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    private val options: Array<String> = arrayOf("Country", "Sources")

    private val countriesMap = mapOf(
        "Any" to null,
        "Australia" to "au",
        "France" to "fr",
        "Germany" to "de",
        "Russia" to "ru",
        "Ukraine" to "ua",
        "USA" to "us"
    )
    private var current_country_pair: Pair<String, String?> = ("USA" to "us")

    private var country_num: Int = 6

//    private var headlinesSelected: Boolean = true

    private var current_category: String? = null

    private var sources: Array<String> = arrayOf("CNN", "TechCrunch", "ABC News")
    private var sources_api: Array<String> = arrayOf("cnn", "techcrunch", "abc-news")
    private var current_checked_sources: BooleanArray = booleanArrayOf(false, false, false)

    private var string_sources: String? = null

    private lateinit var btn_business: Button
    private lateinit var btn_entertainment: Button
    private lateinit var btn_general: Button
    private lateinit var btn_health: Button
    private lateinit var btn_science: Button
    private lateinit var btn_sports: Button
    private lateinit var btn_technology: Button
    private lateinit var btn_options: ImageButton
    private lateinit var searchView: SearchView

    private lateinit var current_view: View

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private lateinit var toolbar: Toolbar
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view =
            LayoutInflater.from(context).inflate(R.layout.fragment_news, container, false)
        current_category = null
//        headlinesSelected = true

        initView()
        initDatabase()
        showNewsHeadlines()

        return current_view
    }

    private fun initView() {
        searchView = current_view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (string_sources != null && current_country_pair.second != null) {
                    Toast.makeText(context, "You can't mix Country and Sources", Toast.LENGTH_SHORT)
                        .show()
                    println("Error In SearchView")
                } else if (string_sources != null) {
                    showNewsHeadlines(
                        category = null,
                        sources = string_sources,
                        query = query,
                        country = null
                    )
                } else if (current_country_pair.second != null) {
                    showNewsHeadlines(
                        category = current_category,
                        sources = null,
                        query = query,
                        country = current_country_pair.second
                    )
                }
                return true
            }

            override fun onQueryTextChange(q: String?): Boolean {
                return false
            }
        })

        toolbar = current_view.findViewById(R.id.toolbar)
        spinner = current_view.findViewById(R.id.spinner)

        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.custom_spinner_item,
            listOf("Headlines", "Everything")
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                p3: Long
            ) {
                if (spinner.selectedItem.toString() == "Everything") {
                    openNewsEverythingFragment()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btn_business = current_view.findViewById(R.id.btn_business)
        btn_entertainment = current_view.findViewById(R.id.btn_entertainment)
        btn_general = current_view.findViewById(R.id.btn_general)
        btn_health = current_view.findViewById(R.id.btn_health)
        btn_science = current_view.findViewById(R.id.btn_science)
        btn_sports = current_view.findViewById(R.id.btn_sports)
        btn_technology = current_view.findViewById(R.id.btn_technology)
        btn_options = current_view.findViewById(R.id.btn_options)

        btn_business.setOnClickListener(this)
        btn_entertainment.setOnClickListener(this)
        btn_general.setOnClickListener(this)
        btn_health.setOnClickListener(this)
        btn_science.setOnClickListener(this)
        btn_sports.setOnClickListener(this)
        btn_technology.setOnClickListener(this)

        btn_options.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> openCountrySettings()
                        1 -> openSourceSettings()
                    }
                }
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openNewsEverythingFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, NewsEverythingFragment())
            .commit()
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

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {  // Bookmarks
                addToBookmarks(item.groupId)
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
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, adapter.headlines[item].url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }

    private fun addToBookmarks(item: Int) {
        val headline = adapter.headlines[item]
        val urlHashCode =
            Hashing.sha1().hashString(headline.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(urlHashCode).setValue(headline)
        Toast.makeText(requireContext(), "Bookmark added", Toast.LENGTH_SHORT).show()
    }

    private fun openCountrySettings() {
        var country_n = country_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Country")
            .setSingleChoiceItems(countriesMap.keys.toTypedArray(), country_num) { dialog, which ->
                println("index: $which")
                country_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                if(country_n != 0 && string_sources != null && string_sources != ""){
                    Toast.makeText(context, "You can't mix Country and Sources, please Reset sources", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    country_num = country_n
                    changeCurrentCountry()

                    showNewsHeadlines(
                        category = current_category,
                        query = null,
                        sources = null,
                        country = current_country_pair.second
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showNewsHeadlines(
        category: String? = null,
        query: String? = null,
        sources: String? = null,
        country: String? = current_country_pair.second
    ) {
        val manager = RequestManager(requireContext())
        manager.getNewsHeadlines(
            listener = listener,
            category = category,
            query = query,
            sources = sources,
            country = country
        )
    }

    fun openSourceSettings() {
        val temp_checked_sources = current_checked_sources
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Source")
            .setMultiChoiceItems(sources, current_checked_sources) { dialog, which, isChecked ->
                temp_checked_sources[which] = isChecked
            }
            .setPositiveButton("Ok") { dialog, which ->
                current_checked_sources = temp_checked_sources
                changeSources()
                if (string_sources != null && current_country_pair.second != null) {
                    Toast.makeText(context, "You can't mix Country and Sources, please set \"Any\" in Country settings", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    showNewsHeadlines(
                        category = null,
                        query = null,
                        country = null,
                        sources = string_sources
                    )
                }
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
        val country_name = countriesMap.keys.toList()[country_num]
        val country_code = countriesMap[country_name]
        current_country_pair = Pair(first = country_name, second = country_code)
        println(current_country_pair)
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
        current_category = button.text.toString()
        val manager = RequestManager(requireContext())
        manager.getNewsHeadlines(
            listener = listener,
            category = current_category,
            query = null,
            sources = null,
            country = current_country_pair.second
        )
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, OpenNewsFragment(headlines))
            //        .addToBackStack(null)  // TODO
            .commit()
    }

    private val listener: OnFetchDataListener<NewsApiResponse> =
        object : OnFetchDataListener<NewsApiResponse> {
            override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
                if (newsHeadlinesList.isEmpty()) {
                    Toast.makeText(requireContext(), "Nothing found", Toast.LENGTH_SHORT).show()
                } else {
                    showNews(newsHeadlinesList)
                }
            }

            override fun onError(message: String) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
        }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        if (context != null) {
            recyclerView = current_view.findViewById(R.id.news_recyclerView)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = NewsAdapter(requireContext(), newsHeadlinesList, this)
            recyclerView.adapter = adapter
        }
    }
}