package com.app.finnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.finnote.adapter.TransactionAdapter
import com.app.finnote.model.Transaction

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val recyclerView = findViewById<RecyclerView>(R.id.rvTransactions)
		val transactions = listOf(
			Transaction("Makan siang", 25000, "2026-04-23"),
			Transaction("Transport", 12000, "2026-04-22"),
			Transaction("Kopi", 18000, "2026-04-21")
		)

		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = TransactionAdapter(transactions)
	}
}