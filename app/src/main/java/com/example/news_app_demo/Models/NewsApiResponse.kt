package com.example.news_app_demo.Models

class NewsApiResponse {
    var status: String = ""
    var totalResults: Int = 0
    lateinit var articles: List<NewsHeadlines>
}