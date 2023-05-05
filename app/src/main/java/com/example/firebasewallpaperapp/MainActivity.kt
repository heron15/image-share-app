package com.example.firebasewallpaperapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.firebasewallpaperapp.Fragment.BookmarkFragment
import com.example.firebasewallpaperapp.Fragment.DownloadFragment
import com.example.firebasewallpaperapp.Fragment.HomeFragment
import com.example.firebasewallpaperapp.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101

    companion object {
        private const val FOLDER_NAME = "MyAppImages"
    }

    private var selectedIcon: ImageView? = null
    private var prevSelectedIcon: ImageView? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar)

        replaceFragment(HomeFragment())
        updateIcon(binding.icHomeImg)

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            createFolder()
        }

        binding.icHome.setOnClickListener {
            replaceFragment(HomeFragment())
            updateIcon(binding.icHomeImg)
        }

        binding.icDownload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            } else {
                // Create folder if permission is granted
                createFolder()
                replaceFragment(DownloadFragment())
                updateIcon(binding.icDownloadImg)
            }
        }

        binding.icBookmark.setOnClickListener {
            replaceFragment(BookmarkFragment())
            updateIcon(binding.icBookmarkImg)
        }

    }

    private fun updateIcon(selectedIcon: ImageView) {
        // set the default color of the icon to white
        val defaultColor = ContextCompat.getColor(this@MainActivity, R.color.white)

        this.selectedIcon?.apply {
            setColorFilter(
                ContextCompat.getColor(this@MainActivity, R.color.black), PorterDuff.Mode.SRC_IN
            )
            // reset the background tint of the previously selected icon
            backgroundTintList = ContextCompat.getColorStateList(
                this@MainActivity, R.color.white
            )
            prevSelectedIcon = this
        }

        this.selectedIcon = selectedIcon
        selectedIcon.apply {
            setColorFilter(
                defaultColor, // set the color to white
                PorterDuff.Mode.SRC_IN
            )
            backgroundTintList = ContextCompat.getColorStateList(
                this@MainActivity, R.color.green
            )
            prevSelectedIcon = this
        }
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentReplaces)

        // Check if the current fragment is not the home fragment
        if (fragment !is HomeFragment) {
            // Navigate to the home fragment
            replaceFragment(HomeFragment())
            updateIcon(binding.icHomeImg)
        } else {
            // If the current fragment is the home fragment, let the system handle the back press
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createFolder()
        } else {
            Toast.makeText(
                this, "Permission not granted. Cannot create folder.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentReplaces, fragment)
        transaction.commit()
    }

    private fun createFolder() {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            FOLDER_NAME
        )

        if (directory.exists()) {
            //Toast.makeText(this, "Folder already exists!", Toast.LENGTH_SHORT).show()
        } else {
            val success = directory.mkdirs()
            if (success) {
                //Toast.makeText(this, "Folder created successfully!", Toast.LENGTH_SHORT).show()
            } else {
                //Toast.makeText(this, "Failed to create folder!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}