package com.example.firebasewallpaperapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.firebasewallpaperapp.databinding.ActivityFinalWallpaperBinding
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.firebasewallpaperapp.Model.BomModel
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL
import java.util.*

class FinalWallpaper : AppCompatActivity() {

    lateinit var binding: ActivityFinalWallpaperBinding
    private val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val url = intent.getStringExtra("link")

        Glide.with(this).load(url).into(binding.finalWallpaper)

        binding.finalWallpaper.maximumScale = 10F;
        binding.finalWallpaper.scaleType = ImageView.ScaleType.CENTER_INSIDE;
        binding.finalWallpaper.setScale(2.0f, true);
        binding.finalWallpaper.setScale(1.0f, true);

        db = FirebaseFirestore.getInstance()

        binding.shareWallpaper.setOnClickListener {
            if (url != null) {
                shareImage(url)
            }
        }

        if (url != null) {
            checkBookmarkStatus(url)
        }

        binding.bookmarkWallpaper.setOnClickListener {
            if (url != null) {
                addBookmark(url)
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

    private fun addBookmark(wallpaperLink: String) {

        db.collection("bookmark")
            .whereEqualTo("link", wallpaperLink)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot!!.isEmpty) {
                        // Data is not a duplicate, so add it to the database
                        val uid = db.collection("bookmark").document().id
                        val finalData = BomModel(uid, wallpaperLink, System.currentTimeMillis())
                        db.collection("bookmark").document(uid).set(finalData)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        this@FinalWallpaper,
                                        "Added to Bookmarks",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Set bookmarked image since the link was added to the bookmark collection
                                    binding.bookmarkWallpaper.setImageResource(R.drawable.bookmark_fill)
                                } else {
                                    Toast.makeText(
                                        this@FinalWallpaper,
                                        "" + it.exception?.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // Data is a duplicate, so remove it from the database
                        for (document in querySnapshot) {
                            db.collection("bookmark").document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@FinalWallpaper,
                                        "Removed from Bookmarks",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Set default image since the link was removed from the bookmark collection
                                    binding.bookmarkWallpaper.setImageResource(R.drawable.bookmark)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@FinalWallpaper,
                                        "" + it.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@FinalWallpaper,
                        "" + task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun checkBookmarkStatus(wallpaperLink: String) {
        db.collection("bookmark")
            .whereEqualTo("link", wallpaperLink)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot!!.isEmpty) {
                        // Data is not a duplicate, so the image is not bookmarked
                        binding.bookmarkWallpaper.setImageResource(R.drawable.bookmark)
                    } else {
                        // Data is a duplicate, so the image is already bookmarked
                        binding.bookmarkWallpaper.setImageResource(R.drawable.bookmark_fill)
                    }
                } else {
                    Toast.makeText(
                        this@FinalWallpaper,
                        "" + task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
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
                                this@FinalWallpaper,
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
                            val progressDialog = Dialog(this@FinalWallpaper)
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
                                    this@FinalWallpaper,
                                    arrayOf(file.absolutePath),
                                    null,
                                    null
                                )

                                // Close the progress dialog
                                progressDialog.dismiss()

                                // Show a toast indicating that the image was saved successfully
                                runOnUiThread {
                                    Toast.makeText(
                                        this@FinalWallpaper,
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
