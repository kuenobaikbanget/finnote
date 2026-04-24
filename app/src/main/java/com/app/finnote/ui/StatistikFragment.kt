package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.github.mikephil.charting.charts.BarChart
import com.app.finnote.ui.chart.TransactionBarChartRenderer
import com.app.finnote.ui.chart.TransactionBarChartRenderer.Mode

class StatistikFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistik, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val barChart = view.findViewById<BarChart>(R.id.barChart)
        TransactionBarChartRenderer.render(barChart, DataStore.transactions, Mode.DETAILED)
    }
}
