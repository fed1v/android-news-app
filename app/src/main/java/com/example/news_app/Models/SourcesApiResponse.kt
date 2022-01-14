package com.example.news_app.Models

import java.io.Serializable

class SourcesApiResponse: Serializable {
    var status: String = ""
    lateinit var sources: List<Source>
}