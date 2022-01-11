package com.example.news_app

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.NewsHeadlines
import com.squareup.picasso.Picasso
import java.lang.Exception

class NewsAdapter(
    var context: Context,
    var headlines: List<NewsHeadlines>,
    var selectListener: SelectListener
) : RecyclerView.Adapter<NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.news_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val headline = headlines[position]

        holder.text_title.setText(headline.title)
        holder.text_source.setText(headline.source?.name)
        holder.text_description.setText(headline.description)

        var time_string: String
        try{
            time_string = headline.publishedAt.substring(0..9) + " " + headline.publishedAt.substring(11..18)
        } catch(e: Exception){
            e.printStackTrace()
            time_string = "unknown"
        }

        holder.text_date.setText(time_string)

        if(headline.urlToImage != null && headline.urlToImage != ""){
            Picasso
                .get()
                .load(headline.urlToImage)
                .resize(2048, 1600)
                .onlyScaleDown()
                .into(holder.img_headline)
        } else{
            holder.img_headline.setImageResource(R.drawable.not_available)  // TODO
        }

        holder.cardView.setOnClickListener{
            selectListener.onNewsClicked(headline)
        }

    }

    override fun getItemCount(): Int = headlines.size
}