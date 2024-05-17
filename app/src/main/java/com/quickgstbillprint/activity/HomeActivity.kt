package com.quickgstbillprint.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quickgstbillprint.R
import com.quickgstbillprint.databinding.ActivityHomeBinding
import com.quickgstbillprint.fragment.BillsFragment
import com.quickgstbillprint.fragment.HomeFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        replaceFragment(BillsFragment())
        initView()
    }

    private fun initView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.parties -> replaceFragment(HomeFragment())
                R.id.bills -> replaceFragment(HomeFragment())
                R.id.items -> replaceFragment(HomeFragment())
                R.id.quickBill -> replaceFragment(BillsFragment())
                R.id.more -> replaceFragment(HomeFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
}