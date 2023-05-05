package com.example.firebasewallpaperapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.firebasewallpaperapp.databinding.ActivityDownloadImageViewBinding
import java.io.File
import java.io.FileOutputStream

class DownloadImageView : AppCompatActivity() {

    lateinit var binding: ActivityDownloadImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val imagePath = intent.getStringExtra("image_path")

        Glide.with(this).load(imagePath).into(binding.downloadImageView)

        binding.downloadImageView.maximumScale = 10F;
        binding.downloadImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE;
        binding.downloadImageView.setScale(2.0f, true);
        binding.downloadImageView.setScale(1.0f, true);

        binding.shareWallpaper.setOnClickListener {
            if (imagePath != null) {
                shareImage(imagePath)
            }
        }

        binding.deleteWallpaper.setOnClickListener {
            deleteStorageImage()
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun deleteStorageImage() {
        val imagePath = intent.getStringExtra("image_path")
        val file = File(imagePath)
        if (file.exists()) {

            val builder = AlertDialog.Builder(this@DownloadImageView)
            val dialogView = layoutInflater.inflate(R.layout.delete_dialog, null)

            builder.setView(dialogView)
            val dialog = builder.create()

            val cancelButton = dialogView.findViewById<TextView>(R.id.cancel_button)
            val deleteButton = dialogView.findViewById<TextView>(R.id.delete_button)

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            deleteButton.setOnClickListener {
                file.delete()
                Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }

            dialog.show()

        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage(imagePath: String) {
        Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    try {
                        val file = File(externalCacheDir, "image.png")
                        val fOut = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                        fOut.flush()
                        fOut.close()
                        file.setReadable(true, false)
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(
                            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                this@DownloadImageView,
                                BuildConfig.APPLICATION_ID + ".provider",
                                file
                            )
                        )
                        intent.type = "image/*"
                        startActivity(Intent.createChooser(intent, "Share Image via"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Do nothing
                }
            })
    }
}