package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.chart.OnMonthSelectedListener
import com.app.finnote.ui.chart.TransactionBarChartRenderer
import com.app.finnote.ui.component.RecentTransactionsView
import com.github.mikephil.charting.charts.BarChart
import java.text.NumberFormat
import java.util.Locale

class TransactionFragment : Fragment(), OnMonthSelectedListener {
    private var recentView: RecentTransactionsView? = null
    private var barChart: BarChart? = null
    private var tvMonthTitle: TextView? = null
    private var tvExpense: TextView? = null
    private var tvIncome: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentView = view.findViewById(R.id.containerRecent)
        recentView?.setFragmentManager(parentFragmentManager, this)
        barChart = view.findViewById(R.id.barChart)
        
        // Card monthly summary views
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle)
        tvExpense = view.findViewById(R.id.tvExpense)
        tvIncome = view.findViewById(R.id.tvIncome)

        
        // Set listener for bar chart month selection
        TransactionBarChartRenderer.setOnMonthSelectedListener(this)
        
        setupUI()

        updateMonthlySummary(TransactionBarChartRenderer.getSelectedMonthKey())
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

    override fun onMonthSelected(monthKey: String, monthIndex: Int) {
        updateMonthlySummary(monthKey)
    }

    private fun updateMonthlySummary(monthKey: String) {
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "Januari"
                "02" -> "Februari"
                "03" -> "Maret"
                "04" -> "April"
                "05" -> "Mei"
                "06" -> "Juni"
                "07" -> "Juli"
                "08" -> "Agustus"
                "09" -> "September"
                "10" -> "Oktober"
                "11" -> "November"
                "12" -> "Desember"
                else -> parts[1]
            }
            tvMonthTitle?.text = getString(R.string.month_year_format, month, year)
        }

        // Get expense and income data
        val expense = DataStore.getExpenseByMonth(monthKey)
        val income = DataStore.getIncomeByMonth(monthKey)

        // Format Rp
        val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        tvExpense?.text = getString(R.string.currency_rp_format, format.format(expense))
        tvIncome?.text = getString(R.string.currency_rp_format, format.format(income))
    }
}
