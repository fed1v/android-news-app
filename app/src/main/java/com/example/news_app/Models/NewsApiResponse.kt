package com.example.news_app.Models

import java.io.Serializable

class NewsApiResponse: Serializable {
    var status: String = ""
    var totalResults: Int = 0
    lateinit var articles: List<NewsHeadlines>
}