package com.app.finnote.ui

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AllTransactionsFragment : Fragment() {
    companion object {
        private const val HEADER_SHRINK_SCROLL_DISTANCE = 140f
        private const val HEADER_SHRINK_AMOUNT = 0.12f
        private const val HEADER_FADE_AMOUNT = 0.08f
        private const val MONTH_KEY_LENGTH = 7
    }

    private val currencyFormatter by lazy {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
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

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddTransactionFragment())
                .addToBackStack(null)
                .commit()
        }

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
        val recyclerView = view.findViewById<RecyclerView>(R.id.allTransactionsRecyclerView)

        title.pivotX = 0f
        title.post { title.pivotY = title.height / 2f }
        backButton.post {
            backButton.pivotX = backButton.width / 2f
            backButton.pivotY = backButton.height / 2f
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollY = recyclerView.computeVerticalScrollOffset()
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
        })
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
        val recyclerView = view.findViewById<RecyclerView>(R.id.allTransactionsRecyclerView)
        val emptyText = view.findViewById<TextView>(R.id.tvAllTransactionsEmpty)
        val transactions = DataStore.getAll()
        val groupedTransactions = groupTransactionsByMonth(transactions)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TransactionsAdapter(buildListItems(groupedTransactions))

        val isEmpty = groupedTransactions.isEmpty()
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions
            .filter { it.date.length >= MONTH_KEY_LENGTH }
            .sortedByDescending { it.date }
            .groupBy { it.date.take(MONTH_KEY_LENGTH) }
    }

    private fun buildListItems(groupedTransactions: Map<String, List<Transaction>>): List<TransactionListItem> {
        return buildList {
            groupedTransactions.entries.forEachIndexed { index, (monthKey, monthTransactions) ->
                add(TransactionListItem.MonthHeader(monthKey, hasTopGap = index > 0))
                monthTransactions.forEach { transaction ->
                    add(TransactionListItem.TransactionRow(transaction))
                }
            }
        }
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

    private fun bindTransactionItem(itemView: View, transaction: Transaction) {
        val tvDate = itemView.findViewById<TextView>(R.id.tvItemDate)
        val tvTitle = itemView.findViewById<TextView>(R.id.tvItemTitle)
        val tvAmount = itemView.findViewById<TextView>(R.id.tvItemAmount)
        val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)

        tvDate.text = formatDate(transaction.date)
        tvTitle.text = transaction.title

        val isIncome = transaction.type == "income"
        val sign = if (isIncome) "+" else "-"
        tvAmount.text = getString(R.string.transaction_amount_format, sign, currencyFormatter.format(transaction.amount))

        val colorRes = if (isIncome) R.color.green else R.color.pale_red
        val typeLabel = getString(if (isIncome) R.string.home_income_label else R.string.home_expense_label)
        val amountText = tvAmount.text.toString()
        val color = ContextCompat.getColor(requireContext(), colorRes)

        tvAmount.setTextColor(color)
        ivIcon?.setColorFilter(color)
        ivIcon?.setImageResource(R.drawable.ic_arrow_down)
        ivIcon?.rotation = if (isIncome) 180f else 0f
        ivIcon?.contentDescription = typeLabel
        itemView.contentDescription = getString(
            R.string.transaction_item_desc,
            transaction.title,
            typeLabel,
            amountText,
            tvDate.text
        )

        itemView.setOnClickListener {
            val detailFragment = TransactionDetailFragment.newInstance(transaction.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }
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

    private sealed class TransactionListItem {
        data class MonthHeader(val monthKey: String, val hasTopGap: Boolean) : TransactionListItem()
        data class TransactionRow(val transaction: Transaction) : TransactionListItem()
    }

    private inner class TransactionsAdapter(
        private val items: List<TransactionListItem>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is TransactionListItem.MonthHeader -> R.layout.item_month_header
                is TransactionListItem.TransactionRow -> R.layout.item_transaction
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return SimpleViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is TransactionListItem.MonthHeader -> {
                    holder.itemView.findViewById<TextView>(R.id.tvMonth).text = formatMonthHeader(item.monthKey)
                    holder.itemView.setPadding(
                        holder.itemView.paddingLeft,
                        if (item.hasTopGap) resources.getDimensionPixelSize(R.dimen.space_lg) else 0,
                        holder.itemView.paddingRight,
                        holder.itemView.paddingBottom
                    )
                }
                is TransactionListItem.TransactionRow -> bindTransactionItem(holder.itemView, item.transaction)
            }
        }

        override fun getItemCount(): Int = items.size
    }

    private class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
