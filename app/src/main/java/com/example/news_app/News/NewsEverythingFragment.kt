package com.example.news_app.News

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.InternetConnection
import com.example.news_app.Helpers.NewsOptionsHelper
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Source
import com.example.news_app.Models.SourcesApiResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NewsEverythingFragment : Fragment(), SelectListener {
    private lateinit var v: View
    private lateinit var toolbar: Toolbar
    private lateinit var spinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var btn_options: ImageButton
    private lateinit var searchView: SearchView

    private val options: Array<String> = arrayOf("Language", "Sources")

    private val countriesMap = NewsOptionsHelper.countries

    private val languagesMap = NewsOptionsHelper.languages
    private var current_language_pair: Pair<String, String?> = ("English" to "en")
    private var language_num: Int = 1

    private var sources_list: MutableList<Source> = mutableListOf()
    private lateinit var sourcesMap: MutableMap<String, String?>
    private var current_checked_sources: BooleanArray = booleanArrayOf()
    private var string_sources: String? = null

    private lateinit var databaseHelper: DatabaseHelper
    private var userPreferences: SharedPreferences? = null

    private var default_category: String? = null
    private var default_language: String? = null
    private var default_country: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context)
            .inflate(R.layout.fragment_news_everything, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }

        initView()
        databaseHelper = DatabaseHelper(requireContext())
        getUserSettings()
        getSources(
            category = default_category,
            language = default_language,
            country = default_country
        )
        showNewsEverything(
            query = null,
            sources = string_sources,
            language = current_language_pair.second
        )
        return v
    }

    private fun getSources(
        category: String? = null,
        language: String? = null,
        country: String? = null
    ) {
        val manager = RequestManager(requireContext())
        manager.getSources(
            listener = sources_listener,
            category = category,
            language = language,
            country = country
        )
    }

    private fun getUserSettings() {
        userPreferences = context?.getSharedPreferences("User settings", Context.MODE_PRIVATE)
        val user_language = userPreferences?.getString("User language", null)
        if (user_language == null) {
            default_language = null
            language_num = 0
        } else {
            default_language = languagesMap[user_language]
            language_num = languagesMap.keys.indexOf(user_language)
        }

        changeCurrentLanguage()

        default_category = userPreferences?.getString("User category", null)
        if (default_category == "Any") {
            default_category = null
        }

        val user_country = userPreferences?.getString("User country", null)
        if (user_country == null) {
            default_country = null
        } else {
            default_country = countriesMap[user_country]
        }

    }

    private fun initView() {
        searchView = v.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showNewsEverything(
                    query = query,
                    sources = string_sources,
                    language = current_language_pair.second
                )
                return true
            }

            override fun onQueryTextChange(q: String?): Boolean {
                return false
            }
        })

        toolbar = v.findViewById(R.id.toolbar)
        spinner = v.findViewById(R.id.spinner)

        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.custom_spinner_item,
            listOf("Everything", "Headlines")
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
                if (spinner.selectedItem.toString() == "Headlines") {
                    openNewsHeadlinesFragment()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btn_options = v.findViewById(R.id.btn_options)
        btn_options.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> openLanguageSettings()
                        1 -> openSourceSettings()
                    }
                }
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openNewsHeadlinesFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, NewsFragment())
            .commit()
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
        databaseHelper.addToBookmarks(headline)
    }

    private fun openLanguageSettings() {
        var language_n = language_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Country")
            .setSingleChoiceItems(languagesMap.keys.toTypedArray(), language_num) { dialog, which ->
                language_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                language_num = language_n
                changeCurrentLanguage()

                showNewsEverything(
                    query = null,
                    sources = string_sources,
                    language = current_language_pair.second
                )
            }
            .setNegativeButton("Cancel") { dialog, which -> }
            .show()
    }

    fun showNewsEverything(
        query: String? = null,
        sources: String? = null,
        language: String? = current_language_pair.second
    ) {
        val manager = RequestManager(requireContext())
        manager.getNewsEverything(
            listener = listener,
            query = query,
            sources = sources,
            language = language
        )
    }

    fun openSourceSettings() {
        sourcesMap = mutableMapOf()
        sources_list.forEach { sourcesMap += Pair(it.name, it.id) }

        val temp_checked_sources = current_checked_sources.copyOf()
        val prev_checked_sources = current_checked_sources.copyOf()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Source")
            .setMultiChoiceItems(
                sourcesMap.keys.toTypedArray(),
                current_checked_sources
            ) { dialog, which, isChecked ->
                temp_checked_sources[which] = isChecked
            }
            .setPositiveButton("Ok") { dialog, which ->
                current_checked_sources = temp_checked_sources.copyOf()
                changeSources()
                showNewsEverything(
                    query = null,
                    sources = string_sources,
                    language = current_language_pair.second
                )
            }
            .setNeutralButton("Reset") { dialog, which ->
                string_sources = null
                for (i in current_checked_sources.indices) current_checked_sources[i] = false
            }
            .setNegativeButton("Cancel") { dialog, which ->
                current_checked_sources = prev_checked_sources.copyOf()
            }
            .setOnCancelListener {
                current_checked_sources = prev_checked_sources.copyOf()
            }
            .show()
    }

    fun changeCurrentLanguage() {
        val language_name = languagesMap.keys.toList()[language_num]
        val language_code = languagesMap[language_name]
        current_language_pair = Pair(first = language_name, second = language_code)
    }

    fun changeSources() {
        string_sources = ""
        val result_sources_list = arrayListOf<String>()
        val sourceIdsArray = sourcesMap.values.toTypedArray()
        for (i in current_checked_sources.indices) {
            if (current_checked_sources[i]) {
                result_sources_list.add(sourceIdsArray[i] ?: "")
            }
        }
        string_sources = TextUtils.join(",", result_sources_list)
        println("String sources: $string_sources")
    }

    override fun onNewsClicked(headlines: NewsHeadlines) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, OpenNewsFragment(headlines))
            .commit()
    }

    private val listener: OnFetchDataListener<NewsApiResponse> =
        object : OnFetchDataListener<NewsApiResponse> {
            override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
                if (newsHeadlinesList.isEmpty()) {
                    if (context != null) {
                        Toast.makeText(context, "Nothing found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showNews(newsHeadlinesList)
                }
            }

            override fun onError(message: String) {
                if (context != null) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val sources_listener: OnFetchSourcesListener<SourcesApiResponse> =
        object : OnFetchSourcesListener<SourcesApiResponse> {
            override fun onFetchSources(sourcesList: List<Source>, message: String) {
                if (sourcesList.isEmpty()) {
                    if (context != null) {
                        Toast.makeText(context, "Sources not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    sources_list.addAll(sourcesList)
                    current_checked_sources = BooleanArray(sources_list.size)
                }
            }

            override fun onError(message: String) {
                if (context != null) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun showNews(newsHeadlinesList: List<NewsHeadlines>) {
        if (context != null) {
            recyclerView = v.findViewById(R.id.news_recyclerView)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = NewsAdapter(requireContext(), newsHeadlinesList, this)
            recyclerView.adapter = adapter
        }
    }
}