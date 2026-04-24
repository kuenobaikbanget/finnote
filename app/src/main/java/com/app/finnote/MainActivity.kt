package com.app.finnote

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.finnote.ui.HistoryFragment
import com.app.finnote.ui.HomeFragment
import com.app.finnote.ui.StatistikFragment
import com.app.finnote.ui.TransactionFragment

class MainActivity : AppCompatActivity() {
    private lateinit var btnNavHome: View
    private lateinit var btnNavTransaction: View
    private lateinit var btnNavStatistik: View
    private lateinit var btnNavHistory: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNavHome = findViewById(R.id.btnNavHome)
        btnNavTransaction = findViewById(R.id.btnNavTransaction)
        btnNavStatistik = findViewById(R.id.btnNavStatistik)
        btnNavHistory = findViewById(R.id.btnNavHistory)

        btnNavHome.setOnClickListener { showTab(HomeFragment()) }
        btnNavTransaction.setOnClickListener { showTab(TransactionFragment()) }
        btnNavStatistik.setOnClickListener { showTab(StatistikFragment()) }
        btnNavHistory.setOnClickListener { showTab(HistoryFragment()) }

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
        btnNavStatistik.isSelected = currentFragment is StatistikFragment
        btnNavHistory.isSelected = currentFragment is HistoryFragment
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}