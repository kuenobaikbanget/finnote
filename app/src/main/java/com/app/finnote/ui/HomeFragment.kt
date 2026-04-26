package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.component.RecentTransactionsView
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var recentView: RecentTransactionsView? = null

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
        val tvBudgetUsage = view.findViewById<TextView>(R.id.tvBudgetUsage)
        val tvBudgetRemaining = view.findViewById<TextView>(R.id.tvBudgetRemaining)
        val progressBudget = view.findViewById<ProgressBar>(R.id.progressBudget)
        recentView = view.findViewById(R.id.containerRecent)
        recentView?.setFragmentManager(parentFragmentManager, this)

        // Add Transaction FAB (currently disabled)
        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
        }

        val user = DataStore.currentUser
        val firstName = user.name.split(" ")[0]
        tvWelcome.text = getString(R.string.welcome_user, firstName)

        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val currentMonthKey = String.format(Locale.US, "%04d-%02d", year, month)

        val totalIncome = DataStore.transactions
            .filter { it.type == "income" && it.date.startsWith(currentMonthKey) }
            .sumOf { it.amount }
        val totalExpense = DataStore.transactions
            .filter { it.type == "expense" && it.date.startsWith(currentMonthKey) }
            .sumOf { it.amount }
        val monthKey = DataStore.getLatestTransactionMonth()
        val monthlyLimit = DataStore.getMonthlyLimit(monthKey)
        val monthlyExpense = DataStore.getExpenseByMonth(monthKey)
        val remainingBudget = (monthlyLimit - monthlyExpense).coerceAtLeast(0)
        val budgetProgress = if (monthlyLimit <= 0) {
            0
        } else {
            ((monthlyExpense.toFloat() / monthlyLimit.toFloat()) * 100f).toInt().coerceIn(0, 100)
        }

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

        tvIncome.text = getString(R.string.summary_income, currencyFormatter.format(totalIncome))
        tvExpense.text = getString(R.string.summary_expense, currencyFormatter.format(totalExpense))
        tvBudgetUsage.text = getString(R.string.budget_usage_format, currencyFormatter.format(monthlyExpense), currencyFormatter.format(monthlyLimit))
        tvBudgetRemaining.text = getString(R.string.budget_remaining_format, currencyFormatter.format(remainingBudget))
        progressBudget.progress = budgetProgress
        recentView?.bindData(DataStore.getAll())
    }

    override fun onResume() {
        super.onResume()
        recentView?.bindData(DataStore.getAll())
    }
}
