package com.app.finnote.ui.chart

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
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

interface OnMonthSelectedListener {
    fun onMonthSelected(monthKey: String, monthIndex: Int)
}

object TransactionBarChartRenderer {
    private var monthKeys = listOf<String>()
    private var selectedMonthIndex = 4
    private var onMonthSelectedListener: OnMonthSelectedListener? = null
    private var barChart: BarChart? = null
    private var incomeDataSet: BarDataSet? = null
    private var expenseDataSet: BarDataSet? = null

    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener) {
        onMonthSelectedListener = listener
    }

    fun clearOnMonthSelectedListener(listener: OnMonthSelectedListener) {
        if (onMonthSelectedListener === listener) {
            onMonthSelectedListener = null
        }
        barChart = null
        incomeDataSet = null
        expenseDataSet = null
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
            val context = chart.context
            val incomeFull = ContextCompat.getColor(context, R.color.green)
            val incomeFade = ColorUtils.setAlphaComponent(incomeFull, 153)
            val expenseFull = ContextCompat.getColor(context, R.color.pale_red)
            val expenseFade = ColorUtils.setAlphaComponent(expenseFull, 153)

            incomeDataSet?.colors = monthKeys.indices.map { i ->
                if (i == selectedMonthIndex) incomeFull else incomeFade
            }
            expenseDataSet?.colors = monthKeys.indices.map { i ->
                if (i == selectedMonthIndex) expenseFull else expenseFade
            }

            val x = selectedMonthIndex.toFloat() + 0.5f
            chart.highlightValues(
                arrayOf(
                    Highlight(x, 0f, 0),
                    Highlight(x, 0f, 1)
                )
            )
            chart.invalidate()
        }
    }

    fun render(
        barChart: BarChart,
        transactions: List<Transaction>,
        animate: Boolean = true
    ) {
        this.barChart = barChart
        val context = barChart.context
        val typeface = ResourcesCompat.getFont(context, R.font.inter)
        val incomeColor = ContextCompat.getColor(context, R.color.green)
        val expenseColor = ContextCompat.getColor(context, R.color.pale_red)
        val mutedTextColor = ContextCompat.getColor(context, R.color.text_muted_accessible)
        val inkColor = ContextCompat.getColor(context, R.color.black)
        val dividerColor = ContextCompat.getColor(context, R.color.divider_mist)

        val monthLabels = mutableListOf<String>()
        val generatedMonthKeys = mutableListOf<String>()
        val labelFormat = SimpleDateFormat("MMM", Locale.forLanguageTag("id-ID"))
        val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)

        for (i in 4 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -i)
            monthLabels.add(labelFormat.format(calendar.time))
            generatedMonthKeys.add(keyFormat.format(calendar.time))
        }
        monthKeys = generatedMonthKeys
        selectedMonthIndex = selectedMonthIndex.coerceIn(monthKeys.indices)

        val incomeEntries = monthKeys.mapIndexed { index, key ->
            BarEntry(
                index.toFloat(),
                transactions
                    .filter { it.type == "income" && it.date.startsWith(key) }
                    .sumOf { it.amount }
                    .toFloat()
            )
        }

        val expenseEntries = monthKeys.mapIndexed { index, key ->
            BarEntry(
                index.toFloat(),
                transactions
                    .filter { it.type == "expense" && it.date.startsWith(key) }
                    .sumOf { it.amount }
                    .toFloat()
            )
        }

        incomeDataSet = BarDataSet(incomeEntries, context.getString(R.string.home_income_label)).apply {
            color = incomeColor
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
            highLightAlpha = 0
        }

        expenseDataSet = BarDataSet(expenseEntries, context.getString(R.string.home_expense_label)).apply {
            color = expenseColor
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
            highLightAlpha = 0
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

                override fun onNothingSelected() = Unit
            })

            typeface?.let {
                xAxis.typeface = it
                axisLeft.typeface = it
                legend.typeface = it
            }

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(monthLabels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = mutedTextColor
                textSize = 11f
                yOffset = 10f
                axisMinimum = 0f
                axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * monthLabels.size
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = dividerColor
                setDrawAxisLine(false)
                textColor = mutedTextColor
                textSize = 11f
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
                textColor = inkColor

                val incomeEntry = com.github.mikephil.charting.components.LegendEntry().apply {
                    label = context.getString(R.string.home_income_label)
                    formColor = incomeColor
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }
                val expenseEntry = com.github.mikephil.charting.components.LegendEntry().apply {
                    label = context.getString(R.string.home_expense_label)
                    formColor = expenseColor
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }
                setCustom(listOf(incomeEntry, expenseEntry))
            }

            xAxis.labelRotationAngle = 0f
            if (animate) {
                animateY(240, com.github.mikephil.charting.animation.Easing.EaseOutQuart)
            }

            updateHighlight()
            invalidate()
        }
    }
}
