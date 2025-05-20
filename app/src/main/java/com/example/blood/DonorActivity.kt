package com.example.blood

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DonorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val headerView = navView.getHeaderView(0)
        val textViewUsername: TextView = headerView.findViewById(R.id.textView_username)
        val textViewUserId: TextView = headerView.findViewById(R.id.textView_userId)
        val textViewBloodGroup: TextView = headerView.findViewById(R.id.textView_bloodGroup)

        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
        FirebaseFirestore.getInstance().collection("Accounts").document(phone).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    textViewUsername.text = "Welcome ${doc.getString("firstName") ?: "User"}"
                    textViewUserId.text = doc.getString("phonenumber") ?: phone
                    textViewBloodGroup.text = doc.getString("bloodGroup") ?: "N/A"
                }
            }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> loadFragment(ProfileFragment(),true)
                R.id.nav_home -> loadFragment(HomeFragment(),true)
                R.id.nav_donation_history -> loadFragment(DonorHistory(),true)
                //R.id.nav_hospital_services -> loadFragment(HospitalServicesFragment())
                //R.id.nav_blood_bank -> loadFragment(BloodBankFragment())
                //R.id.nav_request -> loadFragment(RequestFragment())
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_donate -> loadFragment(CommunityFragment())
                //R.id.nav_request -> loadFragment(ProfileFragment())
            }
            true
        }


        if (savedInstanceState == null) {
            val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber
            if (userId != null) {
                FirebaseFirestore.getInstance().collection("Accounts").document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists() && doc.getString("eligibility") != null) {
                            // Load default home screen
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HomeFragment())
                                .commit()
                        } else {
                            // No profile -> load profile fragment
                            loadFragment(ProfileFragment(), fromDrawer = true)
                        }
                    }
            }
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
