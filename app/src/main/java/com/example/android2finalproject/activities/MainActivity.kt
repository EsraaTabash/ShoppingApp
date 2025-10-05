package com.example.android2finalproject.activities

import android.content.Intent
import com.example.android2finalproject.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.android2finalproject.data.FirestoreService
import com.example.android2finalproject.fragments.AdminDashboardFragment
import com.example.android2finalproject.fragments.CartBadgeHost
import com.example.android2finalproject.fragments.CartFragment
import com.example.android2finalproject.fragments.ShoppingFragment
import com.example.android2finalproject.fragments.StatisticFragment
import com.example.android2finalproject.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() , CartBadgeHost {

    private lateinit var bottomNavigationView: BottomNavigationView
    //fragments
    private val statisticFragment = StatisticFragment()
    private val adminDashboardFragment = AdminDashboardFragment()
    private val shoppingFragment = ShoppingFragment()
    private val profileFragment = ProfileFragment()
    private val cartFragment = CartFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val user = FirebaseAuth.getInstance().currentUser
        // if no user return him to login
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                setupBottomNav(isAdmin)

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "cant decide the role ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                //deault user nav
                setupBottomNav(false)
            }
    }

    //function to handle navigation between fragments
    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    private fun setupBottomNav(isAdmin: Boolean) {
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(
            if (isAdmin) R.menu.bottom_nav_admin else R.menu.bottom_nav_user
        )

        // default fragment based role
        setCurrentFragment(
            if (isAdmin) adminDashboardFragment else shoppingFragment
        )
        bottomNavigationView.selectedItemId = if (isAdmin) R.id.dashboard else R.id.shopping

        bottomNavigationView.setOnItemSelectedListener { item ->
            // stop re-open the same fragment
            if (bottomNavigationView.selectedItemId == item.itemId) return@setOnItemSelectedListener true

            // menu icon -> its own fragment
            when (item.itemId) {
                // admin tabs
                R.id.statistic  -> {
                    if (isAdmin) setCurrentFragment(statisticFragment) else return@setOnItemSelectedListener false
                }
                R.id.dashboard -> {
                    if (isAdmin) setCurrentFragment(adminDashboardFragment) else return@setOnItemSelectedListener false
                }
                R.id.profile -> {
                    setCurrentFragment(profileFragment)
                }

                //user tabs
                R.id.shopping   -> {
                    if (!isAdmin) setCurrentFragment(shoppingFragment) else return@setOnItemSelectedListener false
                }
                R.id.cart       -> {
                    if (!isAdmin) setCurrentFragment(cartFragment) else return@setOnItemSelectedListener false
                }

                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    override fun setCartBadge(count: Long) {
        val badge = bottomNavigationView.getOrCreateBadge(R.id.cart)
        if (count > 0) {
            badge.isVisible = true
            badge.number = count.toInt()
        } else {
            badge.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreService().getCartCount(
            onSuccess = { setCartBadge(it) },
            onError   = {

            }
        )
    }

}
