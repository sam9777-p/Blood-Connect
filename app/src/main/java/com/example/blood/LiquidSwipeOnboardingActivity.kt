package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cuberto.liquid_swipe.LiquidPager

class LiquidSwipeOnboardingActivity : AppCompatActivity() {
    private lateinit var pager: LiquidPager
    private lateinit var skipButton: TextView
    private lateinit var indicatorLayout: LinearLayout
    private val indicators = mutableListOf<TextView>()

    private lateinit var handler: Handler
    private lateinit var autoSlideRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liquid_swipe)

        pager = findViewById(R.id.pager)
        skipButton = findViewById(R.id.btnSkip)
        indicatorLayout = findViewById(R.id.pageIndicatorLayout)

        val adapter = OnboardingPagerAdapter(this)
        pager.adapter = adapter

        setupIndicators(adapter.count)
        highlightIndicator(0)

        skipButton.setOnClickListener {
            navigateToLogin()
        }

        // Auto-slide setup
        handler = Handler(Looper.getMainLooper())
        autoSlideRunnable = object : Runnable {
            override fun run() {
                val nextPage = pager.currentItem + 1
                if (nextPage < adapter.count) {
                    pager.setCurrentItem(nextPage, true)
                    handler.postDelayed(this, 6000)
                }
            }
        }
        handler.postDelayed(autoSlideRunnable, 4000)

        pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                highlightIndicator(position)

                if (position == adapter.count - 1) {
                    handler.removeCallbacks(autoSlideRunnable)

                    val lastPage = pager.findViewWithTag<RelativeLayout>("pager3") // assign tag below
                    val btnGetStarted = lastPage?.findViewById<Button>(R.id.btnGetStarted)
                    btnGetStarted?.visibility = View.VISIBLE
                    btnGetStarted?.setOnClickListener {
                        navigateToLogin()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setupIndicators(count: Int) {
        indicators.clear()
        indicatorLayout.removeAllViews()

        for (i in 0 until count) {
            val dot = TextView(this).apply {
                text = "\u2022"
                textSize = 35f
                setTextColor(ContextCompat.getColor(this@LiquidSwipeOnboardingActivity, android.R.color.darker_gray))
            }
            indicators.add(dot)
            indicatorLayout.addView(dot)
        }
    }

    private fun highlightIndicator(index: Int) {
        for (i in indicators.indices) {
            indicators[i].setTextColor(
                if (i == index)
                    ContextCompat.getColor(this, android.R.color.white)
                else
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginSignupActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoSlideRunnable)
        super.onDestroy()
    }


}
