package com.example.news_app.News

import android.view.ContextMenu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.R

class NewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
    var cardView: CardView
    var text_title: TextView
    var text_source: TextView
    var text_date: TextView
    var text_description: TextView

    var img_headline: ImageView

    init{
        text_title = itemView.findViewById(R.id.text_title)
        text_source = itemView.findViewById(R.id.text_source)
        img_headline = itemView.findViewById(R.id.img_headline)
        cardView = itemView.findViewById(R.id.item_card)
        text_description = itemView.findViewById(R.id.text_description)
        text_date = itemView.findViewById(R.id.text_date)
        cardView.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.add(adapterPosition, 121, 0, "Bookmarks")
        menu?.add(adapterPosition, 122, 1, "Share")
    }
}