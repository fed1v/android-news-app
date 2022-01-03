package com.example.news_app_demo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app_demo.Models.NewsHeadlines
import com.squareup.picasso.Picasso

class CustomAdapter(
    var context: Context,
    var headlines: List<NewsHeadlines>
) : RecyclerView.Adapter<CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(context).inflate(R.layout.headline_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val headLine = headlines.get(position)

        holder.text_title.setText(headLine.title)
        holder.text_source.setText(headLine.source?.name)
        if(headLine.urlToImage != null){
            Picasso.get().load(headLine.urlToImage).into(holder.img_headline)
        } else{
            // TODO
        }
    }

    override fun getItemCount(): Int = headlines.size
}