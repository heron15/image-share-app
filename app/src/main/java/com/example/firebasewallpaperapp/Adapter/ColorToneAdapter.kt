package com.example.firebasewallpaperapp.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasewallpaperapp.FinalWallpaper
import com.example.firebasewallpaperapp.Model.ColorToneModel
import com.example.firebasewallpaperapp.R


class ColorToneAdapter(
    val requireContext: Context,
    val listTheColorTone: ArrayList<ColorToneModel>
) :
    RecyclerView.Adapter<ColorToneAdapter.bomViewHolder>() {

    inner class bomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBack = itemView.findViewById<CardView>(R.id.item_Card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): bomViewHolder {
        return bomViewHolder(
            LayoutInflater.from(requireContext).inflate(R.layout.item_colortone, parent, false)
        )
    }

    override fun getItemCount() = listTheColorTone.size

    override fun onBindViewHolder(holder: bomViewHolder, position: Int) {
        val color = listTheColorTone[position].color
        if (color != null) {
            holder.cardBack.setCardBackgroundColor(Color.parseColor(color))
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(requireContext, FinalWallpaper::class.java)
            intent.putExtra("link",listTheColorTone[position].link)
            requireContext.startActivity(intent)
        }
    }

}