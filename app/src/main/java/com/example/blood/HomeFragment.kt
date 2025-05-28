package com.example.blood

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.blood.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var viewPager: ViewPager2
    private val bannerImages = listOf(
        R.drawable.img2,
        R.drawable.img3,
        R.drawable.banner3
    )

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val scrollRunnable = object : Runnable {
        override fun run() {
            var nextPage = currentPage + 1
            if (nextPage >= bannerAdapter.itemCount) {
                nextPage = 1 // loop back to first real item (index 1)
            }
            viewPager.setCurrentItem(nextPage, true)
            handler.postDelayed(this, 3000)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPagerBanner
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: " "
        val db = FirebaseFirestore.getInstance()

        // Set user data
        db.collection("Accounts").document(phone).get().addOnSuccessListener { doc ->
            doc?.let {
                binding.tvBloodGroup.text = "Blood Group: ${it.getString("bloodGroup")}" ?: "N/A"
                val name = it.getString("firstName") ?: "User"
                val userId = it.getString("userId") ?: phone
                binding.tvWelcome.text = "Welcome $name"
                binding.tvUserId.text = userId
            }
        }

        // ViewPager Setup
        bannerAdapter = BannerAdapter(bannerImages)
        viewPager.adapter = bannerAdapter
        viewPager.setCurrentItem(1, false)
        currentPage = 1

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - kotlin.math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
                page.alpha = 0.7f + r * 0.3f
                page.translationZ = r
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (currentPage) {
                        0 -> viewPager.setCurrentItem(bannerAdapter.itemCount - 2, false) // last real item
                        bannerAdapter.itemCount - 1 -> viewPager.setCurrentItem(1, false) // first real item
                    }
                }
            }
        })


        handler.post(scrollRunnable)

        // QR Code
        binding.qrCodeImage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QRCodeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Request
        binding.requestButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Request())
                .addToBackStack(null)
                .commit()
        }

        // Donor Nearby
        binding.radiodonor.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NearbyHospitals())
                .addToBackStack(null)
                .commit()
        }

        // CTA Button
        binding.btnFindLives.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NearbyHospitals())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(scrollRunnable)
    }
}
