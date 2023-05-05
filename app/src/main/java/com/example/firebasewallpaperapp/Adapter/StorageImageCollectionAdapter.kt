package com.example.firebasewallpaperapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasewallpaperapp.DownloadImageView
import com.example.firebasewallpaperapp.R
import com.makeramen.roundedimageview.RoundedImageView


class StorageImageCollectionAdapter(val requireContext: Context, val listOfCatWallpaper: ArrayList<String>) :
    RecyclerView.Adapter<StorageImageCollectionAdapter.bomViewHolder>() {

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
        val imagePath = listOfCatWallpaper[position]

        Glide.with(requireContext).load(listOfCatWallpaper[position]).into(holder.imageview)

        holder.imageview.setOnClickListener {
            val intent = Intent(requireContext, DownloadImageView::class.java).apply {
                putExtra("image_path", imagePath)
            }
            requireContext.startActivity(intent)
        }
    }

}