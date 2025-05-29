package com.example.blood.ui.donor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.blood.CommunityFragment
import com.example.blood.DonorHistoryFragment

import com.example.blood.HomeFragment
import com.example.blood.LoginSignupActivity
import com.example.blood.ProfileFragment
import com.example.blood.R
import com.example.blood.Request
import com.example.blood.viewmodel.DonorViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class DonorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewModel: DonorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)

        viewModel = ViewModelProvider(this)[DonorViewModel::class.java]

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = navView.getHeaderView(0)
        val textViewUsername: TextView = headerView.findViewById(R.id.textView_username)
        val textViewUserId: TextView = headerView.findViewById(R.id.textView_userId)
        val textViewBloodGroup: TextView = headerView.findViewById(R.id.textView_bloodGroup)

        viewModel.userData.observe(this) { userData ->
            textViewUsername.text = "Welcome ${userData["firstName"] ?: "User"}"
            textViewUserId.text = userData["phonenumber"] ?: ""
            textViewBloodGroup.text = userData["bloodGroup"] ?: "N/A"
        }
        viewModel.loadUserData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> loadFragment(ProfileFragment(), true)
                R.id.nav_home -> loadFragment(HomeFragment(), true)
                R.id.nav_donation_history -> loadFragment(DonorHistoryFragment(), true)
                R.id.nav_request -> loadFragment(Request(), true)
                R.id.nav_logout -> {
                    AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.logout()
                            val intent = Intent(this, LoginSignupActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            finish()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_donate -> loadFragment(CommunityFragment())
                R.id.nav_request -> loadFragment(Request())
            }
            true
        }

        if (savedInstanceState == null) {
            viewModel.isProfileComplete.observe(this) { complete ->
                if (complete) {
                    loadFragment(HomeFragment())
                } else {
                    loadFragment(ProfileFragment(), true)
                }
            }
            viewModel.checkProfileStatus()
        }
    }

    private fun loadFragment(fragment: Fragment, fromDrawer: Boolean = false) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        if (fromDrawer) {
            val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
            for (i in 0 until bottomNav.menu.size()) {
                bottomNav.menu.getItem(i).isChecked = false
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}