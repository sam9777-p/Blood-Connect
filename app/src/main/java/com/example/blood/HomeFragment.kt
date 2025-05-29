package com.example.blood

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.*
import com.example.blood.adapters.BannerAdapter
import com.example.blood.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private val bannerImages = listOf(R.drawable.img2, R.drawable.banner1, R.drawable.banner3)
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    private val scrollRunnable = object : Runnable {
        override fun run() {
            var nextPage = currentPage + 1
            if (nextPage >= bannerAdapter.itemCount) nextPage = 1
            viewPager.setCurrentItem(nextPage, true)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvBloodGroup = view.findViewById<TextView>(R.id.tvBloodGroup)
        val tvUserId = view.findViewById<TextView>(R.id.tvUserId)
        viewPager = view.findViewById(R.id.viewPagerBanner)

        viewModel.fetchUserData()

        viewModel.userName.observe(viewLifecycleOwner, Observer {
            tvWelcome.text = "Welcome $it"
        })

        viewModel.bloodGroup.observe(viewLifecycleOwner, Observer {
            tvBloodGroup.text = "Blood Group: $it"
        })

        viewModel.userId.observe(viewLifecycleOwner, Observer {
            tvUserId.text = it
        })

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

        viewPager.setPageTransformer(transformer)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (currentPage) {
                        0 -> viewPager.setCurrentItem(bannerAdapter.itemCount - 2, false)
                        bannerAdapter.itemCount - 1 -> viewPager.setCurrentItem(1, false)
                    }
                }
            }
        })

        handler.post(scrollRunnable)

        view.findViewById<View>(R.id.qrCodeImage).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QRCodeFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.requestButton).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Request())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.radiodonor).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NearbyHospitals())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnFindLives).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NearbyHospitals())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(scrollRunnable)
    }
}
