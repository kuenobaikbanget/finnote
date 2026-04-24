package com.app.finnote.ui.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.app.finnote.R
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class RecentTransactionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val listContainer: LinearLayout
    private val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_recent_transactions, this, true)
        listContainer = findViewById(R.id.recentListContainer)
    }

    fun bindData(transactions: List<Transaction>, maxItems: Int = 6) {
        listContainer.removeAllViews()

        val items = transactions.takeLast(maxItems).reversed()
        if (items.isEmpty()) {
            val empty = TextView(context).apply {
                text = context.getString(R.string.recent_transactions_empty)
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
            }
            listContainer.addView(empty)
            return
        }

        items.forEachIndexed { index, transaction ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_transaction, listContainer, false)

            val tvDate = itemView.findViewById<TextView>(R.id.tvItemDate)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvItemTitle)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvItemAmount)
            val ivIcon = itemView.findViewById<android.widget.ImageView>(R.id.ivIcon)

            tvDate.text = formatDate(transaction.date)
            tvTitle.text = transaction.title
            tvAmount.text = formatAmount(transaction)
            
            val isIncome = transaction.type == "income"
            val colorHex = if (isIncome) "#7dbe7e" else "#f75c5c"
            
            tvAmount.setTextColor(Color.parseColor(colorHex))
            ivIcon?.setColorFilter(Color.parseColor(colorHex))
            ivIcon?.setImageResource(R.drawable.ic_arrow_down)
            ivIcon?.rotation = if (isIncome) 0f else 180f

            listContainer.addView(itemView)
        }
    }

    private fun formatAmount(transaction: Transaction): String {
        val sign = if (transaction.type == "income") "+" else "-"
        return "$sign ${formatter.format(transaction.amount)}"
    }

    private fun formatDate(date: String): String {
        return try {
            val parsed = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            parsed.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("id-ID")))
        } catch (_: DateTimeParseException) {
            date
        }
    }

}
