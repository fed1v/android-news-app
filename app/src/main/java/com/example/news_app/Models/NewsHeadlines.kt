package com.example.news_app.Models

import java.io.Serializable

data class NewsHeadlines(
    var source: Source? = null,
    var author: String? = "",
    var title: String = "",
    var description: String = "",
    var url: String = "",
    var urlToImage: String? = "",
    var publishedAt: String = "",
    var content: String? = "",
    var category: String? = null
) : Serializable