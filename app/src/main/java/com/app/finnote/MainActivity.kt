package com.app.finnote

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.app.finnote.ui.HomeFragment
import com.app.finnote.ui.ProfileFragment
import com.app.finnote.ui.TransactionFragment

class MainActivity : AppCompatActivity() {
    private lateinit var btnNavHome: View
    private lateinit var btnNavTransaction: View
    private lateinit var btnNavProfile: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<View>(R.id.customBottomNav)

        val navInitialPaddingLeft = bottomNav.paddingLeft
        val navInitialPaddingTop = bottomNav.paddingTop
        val navInitialPaddingRight = bottomNav.paddingRight
        val navInitialPaddingBottom = bottomNav.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(
                navInitialPaddingLeft,
                navInitialPaddingTop,
                navInitialPaddingRight,
                navInitialPaddingBottom + navigationBars.bottom
            )
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

    fun openProfile() {
        showTab(ProfileFragment())
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
}
