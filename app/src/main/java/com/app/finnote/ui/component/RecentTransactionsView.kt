package com.app.finnote.ui.component

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.finnote.R
import com.app.finnote.model.Transaction
import com.app.finnote.ui.AllTransactionsFragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecentTransactionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val listContainer: LinearLayout
    private var fragmentManager: FragmentManager? = null
    private var currentFragment: Fragment? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_recent_transactions, this, true)
        listContainer = findViewById(R.id.recentListContainer)

        val tvSeeAll = findViewById<TextView>(R.id.tvSeeAll)
        tvSeeAll.setOnClickListener {
            fragmentManager?.let { fm ->
                currentFragment?.let { _ ->
                    val allTransactionsFragment = AllTransactionsFragment()
                    fm.beginTransaction()
                        .replace(R.id.fragmentContainer, allTransactionsFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    fun setFragmentManager(fm: FragmentManager, fragment: Fragment) {
        fragmentManager = fm
        currentFragment = fragment
    }

    fun bindData(
        transactions: List<Transaction>,
        maxItems: Int = 6,
        onTransactionClick: ((Transaction) -> Unit)? = null
    ) {
        listContainer.removeAllViews()

        val items = transactions.takeLast(maxItems).reversed()
        if (items.isEmpty()) {
            val empty = TextView(context).apply {
                text = context.getString(R.string.recent_transactions_empty)
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.grey))
            }
            listContainer.addView(empty)
            return
        }

        items.forEachIndexed { _, transaction ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, listContainer, false)

            val tvDate = itemView.findViewById<TextView>(R.id.tvItemDate)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvItemTitle)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvItemAmount)
            val ivIcon = itemView.findViewById<android.widget.ImageView>(R.id.ivIcon)
            val iconContainer = itemView.findViewById<View>(R.id.layoutTransactionIcon)

            tvDate.text = formatDate(transaction.date)
            tvTitle.text = transaction.title
            tvAmount.text = formatAmount(transaction)

            val isIncome = transaction.type == "income"
            val colorRes = if (isIncome) R.color.darker_green else R.color.pale_red
            val tintRes = if (isIncome) R.color.income_tint else R.color.expense_tint
            val typeDescription = if (isIncome) {
                context.getString(R.string.home_income_icon_desc)
            } else {
                context.getString(R.string.home_expense_icon_desc)
            }
            val color = ContextCompat.getColor(context, colorRes)

            tvAmount.setTextColor(color)
            iconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tintRes))
            ivIcon?.setColorFilter(color)
            ivIcon?.setImageResource(R.drawable.ic_arrow_down)
            ivIcon?.rotation = if (isIncome) 0f else 180f
            ivIcon?.contentDescription = typeDescription

            // Handle click to show transaction detail
            itemView.setOnClickListener {
                onTransactionClick?.invoke(transaction)
            }

            listContainer.addView(itemView)
        }
    }

    private fun formatAmount(transaction: Transaction): String {
        val sign = if (transaction.type == "income") "+" else "-"
        return context.getString(R.string.transaction_amount_format, sign, formatter.format(transaction.amount))
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
