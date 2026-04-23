package com.app.finnote.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.finnote.R
import com.app.finnote.model.Transaction

class TransactionAdapter(
	private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

	class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
		val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
		val tvDate: TextView = itemView.findViewById(R.id.tvDate)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_transaction, parent, false)
		return TransactionViewHolder(view)
	}

	override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
		val item = items[position]
		holder.tvTitle.text = item.title
		holder.tvAmount.text = "Rp ${item.amount}"
		holder.tvDate.text = item.date
	}

	override fun getItemCount(): Int = items.size
}