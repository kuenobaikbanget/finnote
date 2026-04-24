package com.app.finnote.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.finnote.R
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class TransactionAdapter(
	private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

	private val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
		maximumFractionDigits = 0
	}

	class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
		val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
		val tvDate: TextView = itemView.findViewById(R.id.tvDate)
		val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_transaction, parent, false)
		return TransactionViewHolder(view)
	}

	override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
		val item = items[position]
		holder.tvTitle.text = item.title
		
		val isIncome = item.type == "income"
		val sign = if (isIncome) "+" else "-"
		holder.tvAmount.text = "$sign ${formatter.format(item.amount)}"
		
		holder.tvDate.text = formatDate(item.date)

		val colorHex = if (isIncome) "#16A34A" else "#DC2626"
		
		holder.tvAmount.setTextColor(Color.parseColor(colorHex))
		holder.ivIcon.setColorFilter(Color.parseColor(colorHex))
		holder.ivIcon.setImageResource(R.drawable.ic_arrow_down)
		holder.ivIcon.rotation = if (isIncome) 0f else 180f
	}

	private fun formatDate(date: String): String {
		return try {
			val parsed = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
			parsed.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("id-ID")))
		} catch (_: DateTimeParseException) {
			date
		}
	}

	override fun getItemCount(): Int = items.size
}