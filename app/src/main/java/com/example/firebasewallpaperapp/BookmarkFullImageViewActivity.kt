package com.example.firebasewallpaperapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.firebasewallpaperapp.databinding.ActivityBookmarkFullImageViewBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

class BookmarkFullImageViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityBookmarkFullImageViewBinding
    private val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkFullImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar)

        val url = intent.getStringExtra("link")
        val documentId = intent.getStringExtra("id")

        Glide.with(this).load(url).into(binding.finalWallpaper)

        binding.finalWallpaper.maximumScale = 10F;
        binding.finalWallpaper.scaleType = ImageView.ScaleType.CENTER_INSIDE;
        binding.finalWallpaper.setScale(2.0f, true);
        binding.finalWallpaper.setScale(1.0f, true);

        db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.shareWallpaper.setOnClickListener {
            if (url != null) {
                shareImage(url)
            }
        }

        binding.alreadyBookmarkWallpaper.setOnClickListener {
            if (documentId != null) {
                db.collection("bookmark").document(documentId)
                    .delete()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "" + it.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        binding.downloadWallpaper.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (url != null) {
                    downloadImage(url)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun shareImage(imageUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
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
                                this@BookmarkFullImageViewActivity,
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

    private fun downloadImage(imageUrl: String) {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png", ".gif", ".bmp")
        val extension = imageUrl.substringAfterLast('.')
        if (!validExtensions.contains(".$extension")) {
            Toast.makeText(
                this,
                "Invalid image URL",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            val random1 = Random().nextInt(520985)
            val random2 = Random().nextInt(520985)
            val name = "AMOLED-${random1 + random2}"
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        try {
                            val directory = File(
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                                ),
                                "MyAppImages"
                            )
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val file = File(directory, "$name.jpg")
                            val fOut = FileOutputStream(file)

                            // Custom progress bar
                            val progressDialog = Dialog(this@BookmarkFullImageViewActivity)
                            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            progressDialog.setCancelable(false)
                            progressDialog.setContentView(R.layout.custom_progress_bar)
                            val progressBar =
                                progressDialog.findViewById<ProgressBar>(R.id.progressBar)
                            val progressText =
                                progressDialog.findViewById<TextView>(R.id.progressText)
                            progressText.text = "0%"

                            // Set up the progress bar's properties
                            progressBar.max = 100
                            progressBar.progress = 0

                            // Start the download in a separate thread
                            Thread {
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                val totalBytes = resource.byteCount
                                var downloadedBytes = 0

                                val inputStream = URL(imageUrl).openStream()
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    fOut.write(buffer, 0, bytesRead)
                                    downloadedBytes += bytesRead
                                    val progress = (downloadedBytes * 100 / totalBytes).toInt()

                                    // Update the progress bar's progress and text
                                    progressBar.progress = progress
                                    runOnUiThread {
                                        progressText.text = "$progress%"
                                    }
                                }

                                // Clean up
                                inputStream.close()
                                fOut.flush()
                                fOut.close()

                                // Refresh the media gallery to show the new image
                                MediaScannerConnection.scanFile(
                                    this@BookmarkFullImageViewActivity,
                                    arrayOf(file.absolutePath),
                                    null,
                                    null
                                )

                                // Close the progress dialog
                                progressDialog.dismiss()

                                // Show a toast indicating that the image was saved successfully
                                runOnUiThread {
                                    Toast.makeText(
                                        this@BookmarkFullImageViewActivity,
                                        "Image saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.start()

                            // Show the progress dialog
                            progressDialog.show()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
        }

    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val url = intent.getStringExtra("link")
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (url != null) {
                    downloadImage(url)
                }
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Unable to download image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}