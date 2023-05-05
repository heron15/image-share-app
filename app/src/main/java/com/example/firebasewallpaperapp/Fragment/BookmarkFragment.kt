package com.example.firebasewallpaperapp.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebasewallpaperapp.Adapter.BookmarkImagesViewAdapter
import com.example.firebasewallpaperapp.Model.BomModel
import com.example.firebasewallpaperapp.R
import com.example.firebasewallpaperapp.databinding.FragmentBookmarkBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class BookmarkFragment : Fragment() {
    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBookmarkBinding.inflate(layoutInflater, container, false)

        reloadData()

        binding.swipe.setOnRefreshListener {
            reloadData()
        }

        return binding.root
    }

    private fun reloadData() {
        if (!isAdded) {
            // The fragment is not attached to a context
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Initialize Firebase and Firestore
        db = FirebaseFirestore.getInstance()

        // Create a reference to the "bookmark" collection
        val bookmarkRef = db.collection("bookmark")

        // Create a list to hold the retrieved data
        val bookmarkList = arrayListOf<BomModel>()

        // Add a listener to the reference to get the data
        bookmarkRef.orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
            if (!isAdded) {
                // The fragment is not attached to a context
                return@addSnapshotListener
            }

            if (exception != null) {
                // Handle any errors
                dialog.dismiss()
                return@addSnapshotListener
            }

            // Clear the list to avoid duplicate entries
            bookmarkList.clear()

            // Add the retrieved data to the list
            snapshot?.forEach { document ->
                val bomModel = document.toObject(BomModel::class.java)
                bookmarkList.add(bomModel)
            }

            binding.rcvBookmark.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.rcvBookmark.adapter = BookmarkImagesViewAdapter(requireContext(), bookmarkList)

            binding.swipe.isRefreshing = false

            dialog.dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
        reloadData()
    }

}
