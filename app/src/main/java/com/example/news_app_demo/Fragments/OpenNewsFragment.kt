package com.example.news_app_demo.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.news_app_demo.Models.NewsHeadlines
import com.example.news_app_demo.R

class OpenNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var curr_view: View
    private lateinit var newsWebView: WebView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        curr_view = inflater.inflate(R.layout.fragment_open_news, container, false)
        newsWebView = curr_view.findViewById(R.id.web_view_news)
        newsWebView.webViewClient = WebViewClient()
        newsWebView.loadUrl(headlines.url)

        toolbar = curr_view.findViewById(R.id.toolbar_open_news)
        toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.mi_share -> shareLink()
                R.id.mi_bookmark -> println("Menu -> Bookmark")
            }
            return@setOnMenuItemClickListener true
        }

        requireActivity().onBackPressedDispatcher.addCallback{
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        return curr_view
    }

    private fun shareLink() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, headlines.url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }
}