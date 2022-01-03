package com.example.news_app_demo

import com.example.news_app_demo.Models.NewsHeadlines

interface SelectListener {
    fun onNewsClicked(headlines: NewsHeadlines)
}