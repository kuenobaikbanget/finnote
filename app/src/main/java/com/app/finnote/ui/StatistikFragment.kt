package com.app.finnote.ui

// Android core
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// Fragment
import androidx.fragment.app.Fragment

// Internal app
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction

// MPAndroidChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

import android.graphics.Color
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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
        val transaction = DataStore.transactions
        setupBarChart(view, transaction)
    }

    private fun setupBarChart(view: View, transactions: List<Transaction>) {
        val barChart = view.findViewById<BarChart>(R.id.barChart)

        val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")
        val monthKeys = listOf("02", "03", "04", "05", "06", "07")

        // Hitung total income & expense per bulan dari DataStore
        val incomeByMonth = monthKeys.map { month ->
            transactions
                .filter { it.type == "income" && it.date.substring(5, 7) == month }
                .sumOf { it.amount }
                .toFloat()
        }

        val expenseByMonth = monthKeys.map { month ->
            transactions
                .filter { it.type == "expense" && it.date.substring(5, 7) == month }
                .sumOf { it.amount }
                .toFloat()
        }

        // Buat entries
        val incomeEntries = incomeByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val expenseEntries = expenseByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

        val incomeDataSet = BarDataSet(incomeEntries, "Income").apply {
            color = Color.parseColor("#7C3AED")
            valueTextColor = Color.TRANSPARENT
        }

        val expenseDataSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.parseColor("#86EFAC")
            valueTextColor = Color.TRANSPARENT
        }

        val groupSpace = 0.2f
        val barSpace = 0.05f
        val barWidth = 0.35f

        val barData = BarData(incomeDataSet, expenseDataSet).apply {
            this.barWidth = barWidth
        }

        barChart.apply {
            data = barData
            groupBars(0f, groupSpace, barSpace)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(months)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                setDrawGridLines(false)
                axisMinimum = 0f
                axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * months.size
            }

            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 100f
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            legend.isEnabled = true
            description.isEnabled = false
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }

}
