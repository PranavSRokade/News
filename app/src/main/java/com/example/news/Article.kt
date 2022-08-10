package com.example.news

import java.io.Serializable

data class Article(
    var author: String?,
    val description: String,
    val publishedAt: String,
    val source: String,
    val title: String,
    val url: String,
    val urlToImage: String
): Serializable