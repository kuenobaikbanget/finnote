package com.app.finnote.ui.chart

import android.graphics.Color
import com.app.finnote.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

object TransactionBarChartRenderer {

    enum class Mode {
        DETAILED,
        SIMPLE
    }

    fun render(
        barChart: BarChart,
        transactions: List<Transaction>,
        mode: Mode
    ) {
        val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")
        val monthKeys = listOf("02", "03", "04", "05", "06", "07")

        val incomeByMonth = monthKeys.map { month ->
            transactions
                .filter { it.type == "income" && getMonthPart(it.date) == month }
                .sumOf { it.amount }
                .toFloat()
        }

        val expenseByMonth = monthKeys.map { month ->
            transactions
                .filter { it.type == "expense" && getMonthPart(it.date) == month }
                .sumOf { it.amount }
                .toFloat()
        }

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
                granularity = if (mode == Mode.DETAILED) 100f else 200f
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            setFitBars(true)

            if (mode == Mode.DETAILED) {
                legend.isEnabled = true
                xAxis.labelRotationAngle = 0f
                animateY(800)
            } else {
                legend.isEnabled = false
                xAxis.labelRotationAngle = -25f
                setDrawValueAboveBar(false)
                setPinchZoom(false)
                setScaleEnabled(false)
                isDoubleTapToZoomEnabled = false
                animateY(500)
            }

            invalidate()
        }
    }

    private fun getMonthPart(date: String): String {
        return if (date.length >= 7) date.substring(5, 7) else ""
    }
}
