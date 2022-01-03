package com.example.news_app_demo

import com.example.news_app_demo.Models.NewsHeadlines

interface OnFetchDataListener<NewsApiResponse> {
    fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String)
    fun onError(message: String)
}