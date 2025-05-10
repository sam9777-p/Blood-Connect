package com.example.blood
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.blood.R

class OnboardingPagerAdapter(private val context: Context) : PagerAdapter() {

    private val layouts = listOf(
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layouts[position], container, false)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = layouts.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}