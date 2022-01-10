package com.example.news_app

import com.example.news_app.Models.NewsHeadlinesStats

interface SelectInStatsListener {
    fun onNewsClicked(headlines: NewsHeadlinesStats)
}