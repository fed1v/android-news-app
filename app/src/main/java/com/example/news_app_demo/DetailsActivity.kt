package com.example.news_app_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.news_app_demo.Models.NewsHeadlines
import com.squareup.picasso.Picasso

class DetailsActivity : AppCompatActivity() {
    lateinit var headlines: NewsHeadlines
    lateinit var txt_title: TextView
    lateinit var txt_author: TextView
    lateinit var txt_time: TextView
    lateinit var txt_detail: TextView
    lateinit var txt_content: TextView
    lateinit var img_news: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        txt_title = findViewById(R.id.text_detail_title)
        txt_author = findViewById(R.id.text_detail_author)
        txt_time = findViewById(R.id.text_detail_time)
        txt_detail = findViewById(R.id.text_detail_detail)
        txt_content = findViewById(R.id.text_detail_content)
        img_news = findViewById(R.id.img_detail_news)

        headlines = intent.getSerializableExtra("data") as NewsHeadlines

        val time_string = headlines.publishedAt.substring(0..9) + " " + headlines.publishedAt.substring(11..18)

        txt_title.setText(headlines.title)
        txt_author.setText(headlines.author)
        txt_time.setText(time_string)
        txt_detail.setText(headlines.description)
        txt_content.setText(headlines.content)
        Picasso.get().load(headlines.urlToImage).into(img_news)
    }
}