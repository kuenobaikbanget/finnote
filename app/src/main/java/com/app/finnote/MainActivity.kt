package com.app.finnote

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.finnote.ui.HomeFragment
import com.app.finnote.ui.ProfileFragment
import com.app.finnote.ui.TransactionFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btnNavHome: View
    private lateinit var btnNavTransaction: View
    private lateinit var btnNavProfile: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<View>(R.id.customBottomNav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        btnNavHome = findViewById(R.id.btnNavHome)
        btnNavTransaction = findViewById(R.id.btnNavTransaction)
        btnNavProfile = findViewById(R.id.btnNavProfile)

        btnNavHome.setOnClickListener { showTab(HomeFragment()) }
        btnNavTransaction.setOnClickListener { showTab(TransactionFragment()) }
        btnNavProfile.setOnClickListener { showTab(ProfileFragment()) }

        if (savedInstanceState == null) {
            showTab(HomeFragment())
        }
    }

    private fun showTab(fragment: Fragment) {
        loadFragment(fragment)
        updateNavSelection(fragment)
    }

    private fun updateNavSelection(currentFragment: Fragment) {
        btnNavHome.isSelected = currentFragment is HomeFragment
        btnNavTransaction.isSelected = currentFragment is TransactionFragment
        btnNavProfile.isSelected = currentFragment is ProfileFragment
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun showTransactionTab() {
        showTab(TransactionFragment())
    }
}
