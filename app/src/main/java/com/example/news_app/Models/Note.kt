package com.example.news_app.Models

import java.io.Serializable

data class Note(
    val title: String? = null,
    val description: String = "",
    val createdTime: Long = 0
): Serializable