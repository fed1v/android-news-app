package com.example.news_app_demo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsHeadlines
import com.squareup.picasso.Picasso

class CustomAdapter(
    var context: Context,
    var headlines: List<NewsHeadlines>,
    var listener: SelectListener
) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(context).inflate(R.layout.headline_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val headline = headlines.get(position)

        holder.text_title.setText(headline.title)
        holder.text_source.setText(headline.source?.name)
        if(headline.urlToImage != null){
            Picasso.get().load(headline.urlToImage).into(holder.img_headline)
        } else{
            holder.img_headline.setImageResource(R.drawable.not_available)
        }

        holder.cardView.setOnClickListener{
            listener.onNewsClicked(headline)
        }

    }

    override fun getItemCount(): Int = headlines.size
}