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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TransactionBarChartRenderer {

    fun render(
        barChart: BarChart,
        transactions: List<Transaction>
    ) {
        val context = barChart.context
        val typeface = ResourcesCompat.getFont(context, R.font.inter)
        
        // Hitung 5 bulan terakhir menggunakan Calendar (kompatibel API 24)
        val monthsLabels = mutableListOf<String>()
        val monthKeys = mutableListOf<String>()
        val sdfLabel = SimpleDateFormat("MMM", Locale.forLanguageTag("id-ID"))
        val sdfKey = SimpleDateFormat("yyyy-MM", Locale.US)

        for (i in 4 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -i)
            monthsLabels.add(sdfLabel.format(calendar.time))
            monthKeys.add(sdfKey.format(calendar.time))
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

        val incomeEntries = incomeByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val expenseEntries = expenseByMonth.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

        val incomeDataSet = BarDataSet(incomeEntries, "Pemasukan").apply {
            color = Color.parseColor("#65c769")
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
        }

        val expenseDataSet = BarDataSet(expenseEntries, "Pengeluaran").apply {
            color = Color.parseColor("#ff6b6c")
            valueTextColor = Color.TRANSPARENT
            setDrawValues(false)
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
                textColor = Color.parseColor("#8a8a8a")
                textSize = 10f
                yOffset = 10f
                
                axisMinimum = 0f
                axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * monthsLabels.size
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
                setDrawAxisLine(false)
                textColor = Color.parseColor("#8a8a8a")
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
                textColor = Color.parseColor("#0d1f2d")
            }

            xAxis.labelRotationAngle = 0f
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

            invalidate()
        }
    }
}
