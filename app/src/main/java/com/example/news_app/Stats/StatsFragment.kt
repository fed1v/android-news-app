package com.example.news_app.Stats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.EncryptionHelper
import com.example.news_app.Helpers.InternetConnection
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.NewsHeadlinesStats
import com.example.news_app.Models.Source
import com.example.news_app.News.NewsFragment
import com.example.news_app.News.OpenNewsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.apache.commons.lang3.time.DurationFormatUtils

class StatsFragment : Fragment(), SelectInStatsListener {
    private lateinit var v: View
    private lateinit var adapter: NewsInStatsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView_total_time: TextView
    private lateinit var btn_all: Button
    private lateinit var btn_business: Button
    private lateinit var btn_entertainment: Button
    private lateinit var btn_general: Button
    private lateinit var btn_health: Button
    private lateinit var btn_other: Button
    private lateinit var btn_science: Button
    private lateinit var btn_sports: Button
    private lateinit var btn_technology: Button
    private lateinit var btn_sources: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var stats: ArrayList<NewsHeadlinesStats>
    private var totalTime: Long = 0

    val categories = listOf(
        "business",
        "entertainment",
        "general",
        "health",
        "science",
        "sports",
        "technology",
        "other"
    )
    private lateinit var current_category: String
    private var current_checked_sources: BooleanArray = booleanArrayOf()
    private lateinit var sourcesSet: MutableSet<Source>
    private lateinit var sourcesMap: MutableMap<String, String?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_stats, container, false)
        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }
        current_category = "all"
        stats = arrayListOf()
        databaseHelper = DatabaseHelper(requireContext())
        initView()
        getTotalTime()
        showTime()
        sourcesSet = mutableSetOf()
        getAllNews()
        return v
    }

    private fun getTotalTime() {
        databaseHelper.userStatsReference.child("total time")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalTime = snapshot.getValue(Long::class.java) ?: 0
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getAllNews() {
        for (category in categories) {
            setCategoryListener(category, false)
        }
        stats.clear()
    }

    private fun setCategoryListener(category: String, clearStats: Boolean = true) {
        val cat: String = if (category == "") "other" else category
        databaseHelper.userStatsReference.child(cat)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (clearStats) {
                        stats.clear()
                    }
                    for (dataSnapshot in snapshot.children) {
                        val headline = dataSnapshot.getValue(NewsHeadlinesStats::class.java)
                        stats.add(headline!!)
                        sourcesSet.add(headline.source!!)
                    }
                    getTotalTime()
                    showNews(stats)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
        val urlHashCode = EncryptionHelper.getSHA1(headline.url)
        databaseHelper.userStatsReference.child(urlHashCode).removeValue()
        Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        btn_all = v.findViewById(R.id.btn_all)
        btn_business = v.findViewById(R.id.btn_business)
        btn_entertainment = v.findViewById(R.id.btn_entertainment)
        btn_general = v.findViewById(R.id.btn_general)
        btn_health = v.findViewById(R.id.btn_health)
        btn_other = v.findViewById(R.id.btn_other)
        btn_science = v.findViewById(R.id.btn_science)
        btn_sports = v.findViewById(R.id.btn_sports)
        btn_technology = v.findViewById(R.id.btn_technology)
        btn_sources = v.findViewById(R.id.btn_sources)

        btn_all.setOnClickListener {
            getAllNews()
        }
        btn_business.setOnClickListener {
            setCategoryListener("business")
        }
        btn_entertainment.setOnClickListener {
            setCategoryListener("entertainment")
        }
        btn_general.setOnClickListener {
            setCategoryListener("general")
        }
        btn_health.setOnClickListener {
            setCategoryListener("health")
        }
        btn_other.setOnClickListener {
            setCategoryListener("other")
        }
        btn_science.setOnClickListener {
            setCategoryListener("science")
        }
        btn_sports.setOnClickListener {
            setCategoryListener("sports")
        }
        btn_technology.setOnClickListener {
            setCategoryListener("technology")
        }
        btn_sources.setOnClickListener {
            openSourceSettings()
        }

        textView_total_time = v.findViewById(R.id.textView_total_time)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)
        recyclerView = v.findViewById(R.id.stats_recyclerView)
        recyclerView.setHasFixedSize(true)

        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.newsFragment
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, NewsFragment())
                ?.commit()
        }
    }

    private fun openSourceSettings() {
        sourcesMap = mutableMapOf()
        if (current_checked_sources.size != sourcesSet.size) {
            current_checked_sources = BooleanArray(sourcesSet.size)
        }

        sourcesSet = sourcesSet.sortedBy { it.name }.toMutableSet()
        sourcesSet.forEach { sourcesMap += Pair(it.name, it.id) }

        val temp_checked_sources = current_checked_sources.copyOf()
        val prev_checked_sources = current_checked_sources.copyOf()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Source")
            .setMultiChoiceItems(
                sourcesMap.keys.toTypedArray(),
                current_checked_sources,
            ) { dialog, which, isChecked ->
                temp_checked_sources[which] = isChecked
            }
            .setPositiveButton("Ok") { dialog, which ->
                current_checked_sources = temp_checked_sources.copyOf()
                val selectedSources = getSelectedSources()
                getStatsWithSelectedSources(selectedSources)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                current_checked_sources = prev_checked_sources.copyOf()
            }
            .setNeutralButton("Reset") { dialog, which ->
                resetSources()
            }
            .setOnCancelListener {
                current_checked_sources = prev_checked_sources.copyOf()
            }
            .show()
    }

    private fun resetSources() {
        for (i in current_checked_sources.indices) {
            current_checked_sources[i] = false
        }
    }

    private fun getSelectedSources(): List<Source> {
        val selectedSources = mutableListOf<Source>()
        val sourcesNamesArray = sourcesMap.keys.toTypedArray()
        val sourcesIdsArray = sourcesMap.values.toTypedArray()
        for (i in sourcesNamesArray.indices) {
            if (current_checked_sources[i]) {
                selectedSources.add(Source(name = sourcesNamesArray[i], id = sourcesIdsArray[i]))
            }
        }
        return selectedSources
    }

    private fun getStatsWithSelectedSources(selectedSources: List<Source>) {
        val resultList = mutableListOf<NewsHeadlinesStats>()
        val selectedSourcesNames = mutableListOf<String>()
        val selectedSourcesIds = mutableListOf<String?>()
        selectedSources.forEach {
            selectedSourcesIds.add(it.id)
            selectedSourcesNames.add(it.name)
        }

        for (news in stats) {
            val id = news.source?.id
            val name = news.source?.name
            if (id != null && id != "" && selectedSourcesIds.contains(id)) {
                resultList.add(news)
            } else if (name != null && name != "" && selectedSourcesNames.contains(name)) {
                resultList.add(news)
            }
        }
        showNews(resultList)
    }

    private fun showNews(news: List<NewsHeadlinesStats>) {
        if (context != null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = NewsInStatsAdapter(requireContext(), news, this)
            recyclerView.adapter = adapter
            showTime()
        }
    }

    private fun showTime() {
        val string_total_time: String =
            DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss", true)
        val time = "Total time: $string_total_time"
        textView_total_time.text = time
    }

    override fun onNewsClicked(headlines: NewsHeadlinesStats) {
        val headlns = NewsHeadlines(
            source = headlines.source,
            author = headlines.author ?: "",
            title = headlines.title,
            description = headlines.description,
            url = headlines.url,
            urlToImage = headlines.urlToImage,
            publishedAt = headlines.publishedAt,
            content = headlines.content ?: "",
            category = headlines.category
        )
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, OpenNewsFragment(headlns))
            .commit()
    }
}