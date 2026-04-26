package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class AllTransactionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Add Transaction FAB
        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
        }

        // Load and display all transactions grouped by month
        loadTransactions(view)
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
