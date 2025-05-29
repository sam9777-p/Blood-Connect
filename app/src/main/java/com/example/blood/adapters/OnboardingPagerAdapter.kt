package com.example.blood.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.viewpager.widget.PagerAdapter
import com.example.blood.LoginSignupActivity
import com.example.blood.R

class OnboardingPagerAdapter(private val context: Context) : PagerAdapter() {

    private val layouts = listOf(
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layouts[position], container, false)

        if (position == layouts.lastIndex) {
            view.tag = "pager3" // ✅ Assign tag for lookup

            val btnGetStarted = view.findViewById<Button>(R.id.btnGetStarted)
            btnGetStarted?.visibility = View.VISIBLE
            btnGetStarted?.setOnClickListener {
                context.startActivity(Intent(context, LoginSignupActivity::class.java))
                (context as? Activity)?.finish()
            }
        }

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