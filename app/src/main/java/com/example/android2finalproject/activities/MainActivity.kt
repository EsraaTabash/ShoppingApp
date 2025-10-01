package com.example.android2finalproject.activities

import android.content.Intent
import com.example.android2finalproject.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.android2finalproject.fragments.AddProductFragment
import com.example.android2finalproject.fragments.ShoppingFragment
import com.example.android2finalproject.fragments.StatisticFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    //three fragments
    private val statisticFragment = StatisticFragment()
    private val addProductFragment = AddProductFragment()
    private val shoppingFragment = ShoppingFragment()


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

        setCurrentFragment(if (isAdmin) statisticFragment else shoppingFragment)
        bottomNavigationView.selectedItemId = if (isAdmin) R.id.statistic else R.id.shopping

        bottomNavigationView.setOnItemSelectedListener { item ->
            // stop re open the same fragment
            if (bottomNavigationView.selectedItemId == item.itemId) return@setOnItemSelectedListener true

            // menu icon -> its own fragment
            when (item.itemId) {
                R.id.statistic  -> setCurrentFragment(statisticFragment)
                R.id.addProduct -> setCurrentFragment(addProductFragment)
                R.id.shopping   -> setCurrentFragment(shoppingFragment)
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }





}
