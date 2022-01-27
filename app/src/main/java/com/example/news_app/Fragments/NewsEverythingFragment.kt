package com.example.news_app.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Source
import com.example.news_app.Models.SourcesApiResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.checkerframework.checker.units.qual.s
import java.nio.charset.Charset

class NewsEverythingFragment : Fragment(), SelectListener /*View.OnClickListener*/ {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    private val options: Array<String> = arrayOf("Language", "Sources")

    private val countriesMap = mapOf(
        "Any" to null,
        "Argentina" to "ar",
        "Australia" to "au",
        "Austria" to "at",
        "Belgium" to "be",
        "Brazil" to "br",
        "Bulgaria" to "bg",
        "Canada" to "ca",
        "China" to "cn",
        "Colombia" to "co",
        "Cuba" to "cu",
        "Czechia" to "cz",
        "Egypt" to "eg",
        "France" to "fr",
        "Germany" to "de",
        "Greece" to "gr",
        "Honk Kong" to "hk",
        "Hungary" to "hu",
        "Indonesia" to "id",
        "Ireland" to "ie",
        "Israel" to "il",
        "India" to "in",
        "Italy" to "it",
        "Japan" to "jp",
        "Korea" to "kr",
        "Latvia" to "lv",
        "Lithuania" to "lt",
        "Morocco" to "ma",
        "Mexico" to "mx",
        "Malaysia" to "my",
        "Nigeria" to "ng",
        "Netherlands" to "nl",
        "Norway" to "no",
        "New Zealand" to "nz",
        "Philippines" to "ph",
        "Poland" to "pl",
        "Portugal" to "pt",
        "Romania" to "ro",
        "Russia" to "ru",
        "Serbia" to "rs",
        "Saudi Arabia" to "sa",
        "Sweden" to "se",
        "Singapore" to "sg",
        "Slovenia" to "si",
        "Slovakia" to "sk",
        "South Africa" to "za",
        "Switzerland" to "ch",
        "Thailand" to "th",
        "Turkey" to "tr",
        "Taiwan" to "tw",
        "Ukraine" to "ua",
        "United Arab Emirates" to "ae",
        "United Kingdom of Great Britain and Northern Ireland" to "gb",
        "USA" to "us",
        "Venezuela" to "ve"
    )

    private val languagesMap = mapOf(
        "Any" to null,
        "Arabic" to "ar",
        "Chinese" to "zh",
        "Dutch" to "nl",
        "English" to "en",
        "French" to "fr",
        "German" to "de",
        "Hebrew" to "he",
        "Italian" to "it",
        "Norwegian" to "no",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Sami" to "se",
        "Spanish" to "es",
    )

    private var current_language_pair: Pair<String, String?> = ("English" to "en")
    private var language_num: Int = 1


    private var sources_list: MutableList<Source> = mutableListOf<Source>()
    private lateinit var sourcesMap: MutableMap<String, String?>
    private var current_checked_sources: BooleanArray = booleanArrayOf()
    private var string_sources: String? = null

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

    private var userPreferences: SharedPreferences? = null

    private var default_category: String? = null
    private var default_language: String? = null
    private var default_country: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        current_view =
            LayoutInflater.from(context)
                .inflate(R.layout.fragment_news_everything, container, false)

        if(!InternetConnection.isConnected()){
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return current_view
        }

        initView()
        initDatabase()

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

        return current_view
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
        searchView = current_view.findViewById(R.id.search_view)
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

        toolbar = current_view.findViewById(R.id.toolbar)
        spinner = current_view.findViewById(R.id.spinner)

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

        btn_options = current_view.findViewById(R.id.btn_options)
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
                .show() //TODO
        }
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
        val urlHashCode =
            Hashing.sha1().hashString(headline.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(urlHashCode).setValue(headline)
        Toast.makeText(requireContext(), "Bookmark added", Toast.LENGTH_SHORT).show()
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
            //        .addToBackStack(null)  // TODO
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
            recyclerView = current_view.findViewById(R.id.news_recyclerView)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = NewsAdapter(requireContext(), newsHeadlinesList, this)
            recyclerView.adapter = adapter
        }
    }
}