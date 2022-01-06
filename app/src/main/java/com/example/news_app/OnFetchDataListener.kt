package com.example.news_app

import com.example.news_app.Models.NewsHeadlines

interface OnFetchDataListener<NewsApiResponse> {
    fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String)
    fun onError(message: String)
}