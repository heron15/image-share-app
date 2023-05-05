package com.example.firebasewallpaperapp

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebasewallpaperapp.Adapter.CatImagesAdapter
import com.example.firebasewallpaperapp.Model.BomModel
import com.example.firebasewallpaperapp.databinding.ActivityCatBinding
import com.google.firebase.firestore.FirebaseFirestore

class CatActivity : AppCompatActivity() {

    lateinit var binding: ActivityCatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val builder = AlertDialog.Builder(this@CatActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        reloadData(dialog)

        binding.swipe.setOnRefreshListener {
            reloadData(dialog)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun reloadData(dialog: AlertDialog) {

        val db = FirebaseFirestore.getInstance()
        val uid = intent.getStringExtra("uid")
        val name = intent.getStringExtra("name")

        db.collection("categories").document(uid!!).collection("wallpaper")
            .addSnapshotListener { value, error ->

                val listOfCatWallpaper = arrayListOf<BomModel>()
                val data = value?.toObjects(BomModel::class.java)?.sortedBy { it.time }
                if (data != null) {
                    listOfCatWallpaper.addAll(data)
                    listOfCatWallpaper.reverse()
                }

                binding.catTitle.text = name.toString()
                binding.catCount.text = "${listOfCatWallpaper.size} Wallpaper Available"

                binding.catRcv.layoutManager =
                    GridLayoutManager(this@CatActivity, 2)
                binding.catRcv.adapter = CatImagesAdapter(this, listOfCatWallpaper)

                binding.swipe.isRefreshing = false
                dialog.dismiss()

            }
    }
}