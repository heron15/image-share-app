package com.example.firebasewallpaperapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasewallpaperapp.BookmarkFullImageViewActivity
import com.example.firebasewallpaperapp.Model.BomModel
import com.example.firebasewallpaperapp.R
import com.makeramen.roundedimageview.RoundedImageView


class BookmarkImagesViewAdapter(val requireContext: Context, val listOfCatWallpaper: MutableList<BomModel>) :
    RecyclerView.Adapter<BookmarkImagesViewAdapter.bomViewHolder>() {

    inner class bomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageview = itemView.findViewById<RoundedImageView>(R.id.catImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): bomViewHolder {
        return bomViewHolder(
            LayoutInflater.from(requireContext).inflate(R.layout.item_wallpaper, parent, false)
        )
    }

    override fun getItemCount() = listOfCatWallpaper.size

    override fun onBindViewHolder(holder: bomViewHolder, position: Int) {
        Glide.with(requireContext).load(listOfCatWallpaper[position].link).into(holder.imageview)
        holder.itemView.setOnClickListener {
            val intent = Intent(requireContext, BookmarkFullImageViewActivity::class.java)
            intent.putExtra("link",listOfCatWallpaper[position].link)
            intent.putExtra("id",listOfCatWallpaper[position].id)
            requireContext.startActivity(intent)
        }
    }

}