package com.app.finnote.ui

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class AllTransactionsFragment : Fragment() {
    companion object {
        private const val HEADER_SHRINK_SCROLL_DISTANCE = 140f
        private const val HEADER_SHRINK_AMOUNT = 0.12f
        private const val HEADER_FADE_AMOUNT = 0.08f
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyHeaderInsets(view)
        setupHeaderScrollMotion(view)

        // Back button
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Add Transaction FAB
        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddTransactionFragment())
                .addToBackStack(null)
                .commit()
        }

        // Load and display all transactions grouped by month
        loadTransactions(view)
    }

    private fun applyHeaderInsets(view: View) {
        val headerContainer = view.findViewById<View>(R.id.headerContainer)
        val initialTopMargin = (headerContainer.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            headerContainer.layoutParams =
                (headerContainer.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = initialTopMargin + (statusBars.top / 2)
                }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun setupHeaderScrollMotion(view: View) {
        if (shouldReduceMotion()) return

        val backButton = view.findViewById<View>(R.id.btnBack)
        val title = view.findViewById<View>(R.id.tvAllTransactionsTitle)
        val scrollView = view.findViewById<NestedScrollView>(R.id.allTransactionsScrollView)

        title.pivotX = 0f
        title.post { title.pivotY = title.height / 2f }
        backButton.post {
            backButton.pivotX = backButton.width / 2f
            backButton.pivotY = backButton.height / 2f
        }

        scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                val progress = (scrollY / HEADER_SHRINK_SCROLL_DISTANCE).coerceIn(0f, 1f)
                val scale = 1f - (HEADER_SHRINK_AMOUNT * progress)
                val alpha = 1f - (HEADER_FADE_AMOUNT * progress)

                title.scaleX = scale
                title.scaleY = scale
                title.alpha = alpha
                backButton.scaleX = scale
                backButton.scaleY = scale
                backButton.alpha = alpha
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

    private fun loadTransactions(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.transactionsContainer)
        container.removeAllViews()

        val transactions = DataStore.getAll()
        val groupedTransactions = groupTransactionsByMonth(transactions)

        if (groupedTransactions.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = getString(R.string.recent_transactions_empty)
                textSize = 16f
                setTextColor("#666666".toColorInt())
                setPadding(0, 32, 0, 0)
            }
            container.addView(emptyText)
            return
        }

        // Display transactions grouped by month
        groupedTransactions.forEach { (monthKey, monthTransactions) ->
            // Add month header
            val headerView = createMonthHeader(monthKey, container)
            container.addView(headerView)

            // Add transactions for this month
            monthTransactions.forEach { transaction ->
                val itemView = createTransactionItem(transaction, container)
                container.addView(itemView)
            }
        }
    }

    private fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions
            .sortedByDescending { it.date }
            .groupBy { it.date.substring(0, 7) } // Group by "YYYY-MM"
    }

    private fun createMonthHeader(monthKey: String, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(requireContext())
        val headerView = inflater.inflate(R.layout.item_month_header, parent, false)

        val tvMonth = headerView.findViewById<TextView>(R.id.tvMonth)
        tvMonth.text = formatMonthHeader(monthKey)

        return headerView
    }

    private fun formatMonthHeader(monthKey: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.US)
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val parsedDate = inputFormat.parse(monthKey)
            if (parsedDate != null) outputFormat.format(parsedDate) else monthKey
        } catch (_: Exception) {
            monthKey
        }
    }

    private fun createTransactionItem(transaction: Transaction, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(requireContext())
        val itemView = inflater.inflate(R.layout.item_transaction, parent, false)

        // Set margin to 8dp as requested
        val layoutParams = itemView.layoutParams as? LinearLayout.LayoutParams
        if (layoutParams != null) {
            val margin8dp = (8 * resources.displayMetrics.density).toInt()
            layoutParams.bottomMargin = margin8dp
            itemView.layoutParams = layoutParams
        }

        val tvDate = itemView.findViewById<TextView>(R.id.tvItemDate)
        val tvTitle = itemView.findViewById<TextView>(R.id.tvItemTitle)
        val tvAmount = itemView.findViewById<TextView>(R.id.tvItemAmount)
        val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

        tvDate.text = formatDate(transaction.date)
        tvTitle.text = transaction.title

        val isIncome = transaction.type == "income"
        val sign = if (isIncome) "+" else "-"
        tvAmount.text = getString(R.string.transaction_amount_format, sign, currencyFormatter.format(transaction.amount))

        val colorHex = if (isIncome) "#7dbe7e" else "#ff6b6c"
        tvAmount.setTextColor(colorHex.toColorInt())
        ivIcon?.setColorFilter(colorHex.toColorInt())
        ivIcon?.setImageResource(R.drawable.ic_arrow_down)
        ivIcon?.rotation = if (isIncome) 0f else 180f

        // Handle click to show transaction detail
        itemView.setOnClickListener {
            val actualIndex = DataStore.transactions.indexOf(transaction)
            if (actualIndex >= 0) {
                val detailFragment = TransactionDetailFragment.newInstance(actualIndex)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return itemView
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID"))
            val parsedDate = inputFormat.parse(date)
            if (parsedDate != null) outputFormat.format(parsedDate) else date
        } catch (_: Exception) {
            date
        }
    }
}
