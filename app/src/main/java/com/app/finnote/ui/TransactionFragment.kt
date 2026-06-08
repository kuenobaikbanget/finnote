package com.app.finnote.ui

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.core.view.WindowInsetsCompat
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
    companion object {
        private const val TITLE_SHRINK_SCROLL_DISTANCE = 140f
        private const val TITLE_SHRINK_AMOUNT = 0.12f
        private const val TITLE_FADE_AMOUNT = 0.08f
    }

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
        applyTransactionInsets(view)
        setupTitleScrollMotion(view)
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

        view.findViewById<View>(R.id.fabAddTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddTransactionFragment())
                .addToBackStack(null)
                .commit()
        }

        updateMonthlySummary(TransactionBarChartRenderer.getSelectedMonthKey())
    }

    private fun applyTransactionInsets(view: View) {
        val titlePage = view.findViewById<View>(R.id.tvTitlePage)
        val initialTopMargin = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            titlePage.layoutParams = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = initialTopMargin + (statusBars.top / 2)
            }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun setupTitleScrollMotion(view: View) {
        if (shouldReduceMotion()) return

        val titlePage = view.findViewById<View>(R.id.tvTitlePage)
        val scrollView = view.findViewById<NestedScrollView>(R.id.transactionScrollView)

        titlePage.pivotX = 0f
        titlePage.post {
            titlePage.pivotY = titlePage.height / 2f
        }

        scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                val progress = (scrollY / TITLE_SHRINK_SCROLL_DISTANCE).coerceIn(0f, 1f)
                val titleScale = 1f - (TITLE_SHRINK_AMOUNT * progress)

                titlePage.scaleX = titleScale
                titlePage.scaleY = titleScale
                titlePage.alpha = 1f - (TITLE_FADE_AMOUNT * progress)
            }
        )
    }

    private fun shouldReduceMotion(): Boolean {
        return try {
            Settings.Global.getFloat(
                requireContext().contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
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
