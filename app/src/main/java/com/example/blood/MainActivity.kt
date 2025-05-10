package com.example.blood
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch", true)

        if (isFirstLaunch) {
            prefs.edit().putBoolean("first_launch", false).apply()
            startActivity(Intent(this, LiquidSwipeOnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, LoginSignupActivity::class.java))
        }

        finish()
    }
}