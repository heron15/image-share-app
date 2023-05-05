package com.example.firebasewallpaperapp.Fragment

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.example.firebasewallpaperapp.Adapter.BomAdapter
import com.example.firebasewallpaperapp.Adapter.CatItemAdapter
import com.example.firebasewallpaperapp.Adapter.ColorToneAdapter
import com.example.firebasewallpaperapp.Model.BomModel
import com.example.firebasewallpaperapp.Model.CatModel
import com.example.firebasewallpaperapp.Model.ColorToneModel
import com.example.firebasewallpaperapp.R
import com.example.firebasewallpaperapp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var db: FirebaseFirestore
    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding.btnMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
            } else {
                binding.drawerLayout.openDrawer(Gravity.LEFT)
            }
        }

        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.more -> {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/developer?id=Ideal+studio")
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/developer?id=Ideal+studio")
                            )
                        )
                    }
                    true
                }
                R.id.rate -> {
                    val uri = Uri.parse("market://details?id=")
                    val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        startActivity(myAppLinkToMarket)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            " Unable to find Market App",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    true
                }

                R.id.link -> {
                    val browserInternet = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://idealstudioappprivacypolicy.blogspot.com/2022/09/internet-packages-offer.html")
                    )
                    startActivity(browserInternet)

                    Toast.makeText(requireContext(), "Read Privacy Policy", Toast.LENGTH_SHORT)
                        .show()
                    true
                }

                R.id.contact -> {
                    Toast.makeText(requireContext(), "Contact", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false

            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.let {
            val builder = AlertDialog.Builder(requireContext())
            builder.setCancelable(false)
            builder.setView(ProgressBar(requireContext()))
            dialog = builder.create()
        }
        if (savedInstanceState == null) {
            dialog?.show()
            loadBomData()
            loadColorToneData()
            loadCategoryData()
        }
    }

    private fun loadCategoryData() {
        db.collection("categories").addSnapshotListener { value, error ->

            if (value != null) {
                val listOfCategory = arrayListOf<CatModel>()
                val data = value.toObjects(CatModel::class.java)?.sortedBy { it.time }
                if (data != null) {
                    listOfCategory.addAll(data)
                    listOfCategory.reverse()
                }

                binding.rcvCat.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.rcvCat.adapter = CatItemAdapter(requireContext(), listOfCategory)

                dialog?.dismiss()
            } else {
                Log.e(TAG, "Error getting categories: ", error)
            }
        }
    }


    private fun loadColorToneData() {
        db.collection("thecolortone").addSnapshotListener { value, error ->

            val listTheColorTone = arrayListOf<ColorToneModel>()
            val data = value?.toObjects(ColorToneModel::class.java)?.sortedBy { it.time }
            if (data != null) {
                listTheColorTone.addAll(data)
                listTheColorTone.reverse()
            }

            binding.rcvTct.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rcvTct.adapter = ColorToneAdapter(requireContext(), listTheColorTone)

            dialog?.dismiss()
        }
    }

    private fun loadBomData() {
        val context = requireContext()
        db.collection("bestofmonth").addSnapshotListener { value, error ->
            val listBestOfMonth = arrayListOf<BomModel>()
            val data = value?.toObjects(BomModel::class.java)?.sortedBy { it.time }
            if (data != null) {
                listBestOfMonth.addAll(data)
                listBestOfMonth.reverse()
            }

            binding.rcvBom.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rcvBom.adapter = BomAdapter(context, listBestOfMonth)

            // Set up auto-scroll
            val handler = Handler()
            val runnable = object : Runnable {
                override fun run() {
                    val layoutManager = binding.rcvBom.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val smoothScroller = object : LinearSmoothScroller(context) {
                        override fun getHorizontalSnapPreference(): Int {
                            return SNAP_TO_END // change to SNAP_TO_END
                        }

                        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                            return 150f / (displayMetrics?.densityDpi
                                ?: DisplayMetrics.DENSITY_DEFAULT) * DisplayMetrics.DENSITY_DEFAULT / (displayMetrics?.densityDpi
                                ?: DisplayMetrics.DENSITY_DEFAULT)
                        }
                    }
                    if (lastVisibleItemPosition < layoutManager.itemCount - 1) {
                        smoothScroller.targetPosition = lastVisibleItemPosition + 1
                    } else {
                        smoothScroller.targetPosition = 0
                    }
                    binding.rcvBom.layoutManager?.startSmoothScroll(smoothScroller)
                    handler.postDelayed(this, 3000)
                }
            }
            handler.postDelayed(runnable, 3000)

            dialog?.dismiss()
        }
    }

    //for back
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.exit_dialog, null)

                    builder.setView(dialogView)
                    val dialog = builder.create()

                    val yesButton = dialogView.findViewById<TextView>(R.id.yes_button)
                    val noButton = dialogView.findViewById<TextView>(R.id.no_button)

                    yesButton.setOnClickListener {
                        isEnabled = false
                        requireActivity().finish()
                    }

                    noButton.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        dialog = null
    }

}