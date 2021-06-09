package com.project.noteapp.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.noteapp.R
import com.project.noteapp.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*

class NotesAdapter(private val context: Context, private val list: List<Notes>) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val desc: TextView = itemView.findViewById(R.id.tvDesc)
        val date: TextView = itemView.findViewById(R.id.tvDateTime)

        fun bind(data: Notes) {
            title.text = data.title.toString()
            desc.text = data.noteText.toString()
            date.text = data.dateTime.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_rv_notes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        if (list[position].color != null) {
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(list[position].color))
        } else {
            holder.itemView.cardView.setCardBackgroundColor(Color.BLACK)
        }

        if(list[position].imgPath != null) {
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(list[position].imgPath))
            holder.itemView.imgNote.visibility = View.VISIBLE
        }else {
            holder.itemView.imgNote.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size
}