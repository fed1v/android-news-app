package com.example.news_app

import com.example.news_app.Models.NewsHeadlines

interface SelectListener {
    fun onNewsClicked(headlines: NewsHeadlines)
}