package com.example.news_app

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.NewsHeadlinesStats
import com.squareup.picasso.Picasso
import org.apache.commons.lang3.time.DurationFormatUtils
import java.lang.Exception

class NewsInStatsAdapter(
    var context: Context,
    var headlines: List<NewsHeadlinesStats>,
    var selectListener: SelectInStatsListener
) : RecyclerView.Adapter<NewsInStatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsInStatsViewHolder {
        return NewsInStatsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.news_in_stats_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewsInStatsViewHolder, position: Int) {
        val headline = headlines[position]

        holder.text_title.setText(headline.title)
        holder.text_source.setText(headline.source?.name)
        holder.text_description.setText(headline.description)

        var publishedAt_string: String
        try{
            publishedAt_string = headline.publishedAt.substring(0..9) + " " + headline.publishedAt.substring(11..18)
        } catch(e: Exception){
            e.printStackTrace()
            publishedAt_string = "unknown"
        }

        holder.text_date.setText(publishedAt_string)

        val time_hhmmss = DurationFormatUtils.formatDuration(headline.time, "HH:mm:ss", true)
        val time = "User time: $time_hhmmss"
        holder.text_time.setText(time)

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