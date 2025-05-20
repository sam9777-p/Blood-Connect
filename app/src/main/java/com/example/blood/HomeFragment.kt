// HomeFragment.kt
package com.example.blood

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner3
    )

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (currentPage == bannerImages.size) currentPage = 0
            viewPager.setCurrentItem(currentPage++, true)
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
        var qrCodeImage = binding.qrCodeImage
        val BloodGroup = binding.tvBloodGroup
        val donor = binding.radiodonor
        val tvWelcome = binding.tvWelcome
        val tvUserId = binding.tvUserId
        donor.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, NearbyHospitals())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        qrCodeImage.setOnClickListener {
            val qrCodeFragment = QRCodeFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, qrCodeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val phone=FirebaseAuth.getInstance().currentUser?.phoneNumber ?:" "
        FirebaseFirestore.getInstance().collection("Accounts").document(phone).get().addOnSuccessListener { data ->
            data?.let {
                BloodGroup.text = it.getString("bloodGroup") ?: "N/A"
                val name = it.getString("firstName") ?: "User"
                val userId = it.getString("userId") ?: phone
                tvWelcome.text = "Welcome $name"
                tvUserId.text = userId
            }

        }
        bannerAdapter = BannerAdapter(bannerImages)
        viewPager.adapter = bannerAdapter
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3
        viewPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        handler.post(scrollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(scrollRunnable)
    }
}
