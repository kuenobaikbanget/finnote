package com.app.finnote.ui.chart

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.app.finnote.R
import com.app.finnote.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.graphics.toColorInt

interface OnMonthSelectedListener {
    fun onMonthSelected(monthKey: String, monthIndex: Int)
}

object TransactionBarChartRenderer {
    private var monthKeys = listOf<String>()
    private var selectedMonthIndex = 4 // Default to current month (last in list)
    private var onMonthSelectedListener: OnMonthSelectedListener? = null
    private var barChart: BarChart? = null
    private var incomeEntries = listOf<BarEntry>()
    private var expenseEntries = listOf<BarEntry>()
    private var incomeDataSet: BarDataSet? = null
    private var expenseDataSet: BarDataSet? = null

    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener) {
        onMonthSelectedListener = listener
    }

    fun getSelectedMonthKey(): String {
        return if (selectedMonthIndex in monthKeys.indices) {
            monthKeys[selectedMonthIndex]
        } else {
            monthKeys.lastOrNull() ?: "2026-04"
        }
    }

    private fun updateHighlight() {
        barChart?.let { chart ->
            // Update colors for fade effect
            val incomeFull = "#65c769".toColorInt()
            val incomeFade = "#9965c769".toColorInt() // ~60% opacity
            val expenseFull = "#ff6b6c".toColorInt()
            val expenseFade = "#99ff6b6c".toColorInt() // ~60% opacity

            val incomeColors = monthKeys.indices.map { i ->
                if (i == selectedMonthIndex) incomeFull else incomeFade
            }
            val expenseColors = monthKeys.indices.map { i ->
                if (i == selectedMonthIndex) expenseFull else expenseFade
            }

            incomeDataSet?.colors = incomeColors
            expenseDataSet?.colors = expenseColors

            val highlights = mutableListOf<Highlight>()
            val x = selectedMonthIndex.toFloat() + 0.5f
            
            // Income bar entry (dataSetIndex 0)
            highlights.add(Highlight(x, 0f, 0))
            // Expense bar entry (dataSetIndex 1)
            highlights.add(Highlight(x, 0f, 1))
            
            chart.highlightValues(highlights.toTypedArray())
            chart.invalidate()
        }
    }

    fun render(
        barChart: BarChart,
        transactions: List<Transaction>
    ) {
        this.barChart = barChart
        val context = barChart.context
        val typeface = ResourcesCompat.getFont(context, R.font.inter)
        
        // Hitung 5 bulan terakhir menggunakan Calendar
        val monthsLabels = mutableListOf<String>()
        monthKeys = mutableListOf()
        val sdfLabel = SimpleDateFormat("MMM", Locale.forLanguageTag("id-ID"))
        val sdfKey = SimpleDateFormat("yyyy-MM", Locale.US)

        for (i in 4 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -i)
            monthsLabels.add(sdfLabel.format(calendar.time))
            (monthKeys as MutableList<String>).add(sdfKey.format(calendar.time))
        }

        val incomeByMonth = monthKeys.map { key ->
            transactions
                .filter { it.type == "income" && it.date.startsWith(key) }
                .sumOf { it.amount }
                .toFloat()
        }

        val expenseByMonth = monthKeys.map { key ->
            transactions
                .filter { it.type == "expense" && it.date.startsWith(key) }
                .sumOf { it.amount }
                .toFloat()
        }

        incomeEntries = incomeByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        expenseEntries = expenseByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

        incomeDataSet = BarDataSet(incomeEntries, "Pemasukan").apply {
            color = "#65c769".toColorInt()
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
            highLightAlpha = 0 // Remove dark overlay
        }

        expenseDataSet = BarDataSet(expenseEntries, "Pengeluaran").apply {
            color = "#ff6b6c".toColorInt()
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
            highLightAlpha = 0 // Remove dark overlay
        }

        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f

        val barData = BarData(incomeDataSet, expenseDataSet).apply {
            this.barWidth = barWidth
        }

        barChart.apply {
            data = barData
            xAxis.axisMinimum = 0f
            groupBars(0f, groupSpace, barSpace)

            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(false)
            setDrawBorders(false)
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            setScaleEnabled(false)

            extraBottomOffset = 20f

            // Set up click listener
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    h?.let {
                        val index = it.x.toInt()
                        if (index in monthKeys.indices) {
                            selectedMonthIndex = index
                            updateHighlight()
                            onMonthSelectedListener?.onMonthSelected(monthKeys[index], index)
                        }
                    }
                }

                override fun onNothingSelected() {
                    // Keep the current selection
                }
            })

            typeface?.let {
                xAxis.typeface = it
                axisLeft.typeface = it
                legend.typeface = it
            }

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(monthsLabels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = "#8a8a8a".toColorInt()
                textSize = 10f
                yOffset = 10f
                
                axisMinimum = 0f
                axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * monthsLabels.size
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = "#F0F0F0".toColorInt()
                setDrawAxisLine(false)
                textColor = "#8a8a8a".toColorInt()
                textSize = 10f
                xOffset = 10f
                axisMinimum = 0f

                granularity = 500000f
                isGranularityEnabled = true

                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000000f -> {
                                val millions = value / 1000000f
                                if (millions % 1 == 0f) "${millions.toInt()}jt" 
                                else "${String.format(Locale.US, "%.1f", millions).replace(".", ",")}jt"
                            }
                            value >= 1000f -> "${(value / 1000f).toInt()}rb"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }
            
            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                yOffset = 10f
                form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                textSize = 11f
                textColor = "#0d1f2d".toColorInt()


                val incomeEntry = com.github.mikephil.charting.components.LegendEntry().apply {
                    label = "Pemasukan"
                    formColor = "#65c769".toColorInt()
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }
                val expenseEntry = com.github.mikephil.charting.components.LegendEntry().apply {
                    label = "Pengeluaran"
                    formColor = "#ff6b6c".toColorInt()
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }
                setCustom(listOf(incomeEntry, expenseEntry))
            }

            xAxis.labelRotationAngle = 0f
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

            updateHighlight()
            invalidate()
        }
    }
}
