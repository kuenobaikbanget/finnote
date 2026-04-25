package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.chart.TransactionBarChartRenderer
import com.app.finnote.ui.component.RecentTransactionsView
import com.github.mikephil.charting.charts.BarChart

class TransactionFragment : Fragment() {
    private var recentView: RecentTransactionsView? = null
    private var barChart: BarChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentView = view.findViewById(R.id.containerRecent)
        barChart = view.findViewById(R.id.barChart)
        
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        val transactions = DataStore.getAll()
        recentView?.bindData(transactions)
        barChart?.let {
            TransactionBarChartRenderer.render(it, transactions)
        }
    }
}
