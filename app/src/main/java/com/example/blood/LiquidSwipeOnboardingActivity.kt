package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cuberto.liquid_swipe.LiquidPager


class LiquidSwipeOnboardingActivity : AppCompatActivity() {
    private lateinit var pager: LiquidPager
    private lateinit var skipButton: TextView
    private lateinit var indicatorLayout: LinearLayout
    private val indicators = mutableListOf<TextView>()

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

        pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                highlightIndicator(position)
                if (position == adapter.count - 1) {
                    navigateToLogin()
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
}


