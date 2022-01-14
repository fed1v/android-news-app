package com.example.news_app

import com.example.news_app.Models.Source

interface OnFetchSourcesListener<SourcesApiResponse> {
    fun onFetchSources(sourcesList: List<Source>, message: String)
    fun onError(message: String)
}