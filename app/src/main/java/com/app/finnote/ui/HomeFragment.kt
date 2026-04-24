package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.finnote.MainActivity
import com.app.finnote.R
import com.app.finnote.data.DataStore
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvIncome = view.findViewById<TextView>(R.id.tvIncome)
        val tvExpense = view.findViewById<TextView>(R.id.tvExpense)
        val tvBalance = view.findViewById<TextView>(R.id.tvBalance)
        val btnGoToAdd = view.findViewById<Button>(R.id.btnGoToAdd)

        val userName = getString(R.string.default_user_name)
        tvWelcome.text = getString(R.string.welcome_user, userName)

        val totalIncome = DataStore.transactions
            .filter { it.type == "income" }
            .sumOf { it.amount }
        val totalExpense = DataStore.transactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

        tvIncome.text = getString(R.string.summary_income, currencyFormatter.format(totalIncome))
        tvExpense.text = getString(R.string.summary_expense, currencyFormatter.format(totalExpense))
        tvBalance.text = getString(R.string.summary_balance, currencyFormatter.format(totalBalance))

        btnGoToAdd.setOnClickListener {
            (activity as? MainActivity)?.showTransactionTab()
        }
    }
}
