package com.example.news_app_demo.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import com.example.news_app_demo.Models.NewsHeadlines
import com.example.news_app_demo.R

class OpenNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var curr_view: View
    private lateinit var newsWebView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        curr_view = inflater.inflate(R.layout.fragment_open_news, container, false)
        newsWebView = curr_view.findViewById(R.id.web_view_news)
        newsWebView.webViewClient = WebViewClient()
        newsWebView.loadUrl(headlines.url)


        requireActivity().onBackPressedDispatcher.addCallback{
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NewsFragment())
                .commit()
        }

        return curr_view
    }


}