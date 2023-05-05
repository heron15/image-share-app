package com.example.firebasewallpaperapp.Fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebasewallpaperapp.Adapter.StorageImageCollectionAdapter
import com.example.firebasewallpaperapp.databinding.FragmentDownloadBinding
import java.io.File


class DownloadFragment : Fragment() {

    lateinit var binding: FragmentDownloadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDownloadBinding.inflate(layoutInflater, container, false)

        reloadData()

        binding.swipe.setOnRefreshListener {
            reloadData()
        }

        return binding.root
    }

    private fun reloadData() {

        val imageList = arrayListOf<String>()

        val targetPath =
            Environment.getExternalStorageDirectory().absolutePath + "/Pictures/MyAppImages"

        val targetFile = File(targetPath)

        // Get all files in the target directory and sort them by their last modified time
        val allFiles = targetFile.listFiles()?.sortedByDescending { it.lastModified() }

        allFiles?.let {
            for (data in it) {
                imageList.add(data.absolutePath)
            }
        }

        binding.rcvCollection.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.rcvCollection.adapter = StorageImageCollectionAdapter(requireContext(), imageList)

        binding.swipe.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }
}