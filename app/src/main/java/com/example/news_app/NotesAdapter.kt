package com.example.news_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.Note
import kotlinx.android.synthetic.main.notes_item.view.*
import java.text.DateFormat

class NotesAdapter(
    var context: Context,
    var notes: List<Note>,
    var selectListener: SelectInNotesListener
    ): RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_item, parent, false))
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = notes[position]

        holder.itemView.text_title.text = note.title
        holder.itemView.text_description.text = note.description

        val stringTime = DateFormat.getDateTimeInstance().format(note.createdTime)
        holder.itemView.text_time.text = stringTime

        holder.itemView.cardView.setOnClickListener {
            selectListener.onNoteClicked(note)
        }
    }

    override fun getItemCount(): Int = notes.size


    class NotesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private var cardView: CardView
        private var text_title: TextView
        private var text_description: TextView
        private var text_time: TextView

        init{
            text_title = itemView.findViewById(R.id.text_title)
            text_description = itemView.findViewById(R.id.text_description)
            text_time = itemView.findViewById(R.id.text_time)
            cardView = itemView.findViewById(R.id.cardView)
        }
    }
}