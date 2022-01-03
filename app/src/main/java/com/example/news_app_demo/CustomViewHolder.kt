package com.example.news_app_demo

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CustomViewHolder(var itemView: View): RecyclerView.ViewHolder(itemView) {
    var cardView: CardView
    var text_title: TextView
    var text_source: TextView
    var img_headline: ImageView

    init{
        text_title = itemView.findViewById(R.id.text_title)
        text_source = itemView.findViewById(R.id.text_source)
        img_headline = itemView.findViewById(R.id.img_headline)
        cardView = itemView.findViewById(R.id.item_card)
    }
}