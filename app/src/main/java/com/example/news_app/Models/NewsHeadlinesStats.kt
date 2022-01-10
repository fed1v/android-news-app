package com.example.news_app.Models

import java.io.Serializable

data class NewsHeadlinesStats(
    var source: Source? = null,
    var author: String = "",
    var title: String = "",
    var description: String = "",
    var url: String = "",
    var urlToImage: String? = "",
    var publishedAt: String = "",
    var content: String? = "",
    var time: Long = 0
) : Serializable {
    constructor(headlines: NewsHeadlines, time: Long) : this(
        source = headlines.source,
        author = headlines.author,
        title = headlines.title,
        description = headlines.description,
        url = headlines.url,
        urlToImage = headlines.urlToImage,
        publishedAt = headlines.publishedAt,
        content = headlines.content,
        time = time
    )
}