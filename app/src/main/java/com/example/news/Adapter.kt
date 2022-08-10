package com.example.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class Adapter(newsList: ArrayList<Article>): RecyclerView.Adapter<Adapter.ViewHolder>(){
    val newsList: ArrayList<Article>
    val originalNewsList: ArrayList<Article>

    init{
        this.newsList = newsList
        originalNewsList = newsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemPublishedAt.setText(newsList[position].publishedAt + " hours ago")
        holder.itemSource.setText(newsList[position].source)
        holder.itemTitle.setText(newsList[position].title)
        holder.itemDescription.setText(newsList[position].description)
        Picasso.get().load(newsList[position].urlToImage).fit().centerCrop().into(holder.itemImage)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemPublishedAt: TextView
        var itemSource: TextView
        var itemTitle: TextView
        var itemDescription: TextView
        var itemImage: ImageView

        init{
            itemPublishedAt = itemView.findViewById(R.id.publishedAt)
            itemSource = itemView.findViewById(R.id.source)
            itemTitle = itemView.findViewById(R.id.title)
            itemDescription = itemView.findViewById(R.id.description)
            itemImage = itemView.findViewById(R.id.image)
        }
    }
}
