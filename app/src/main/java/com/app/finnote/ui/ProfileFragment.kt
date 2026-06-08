package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyProfileInsets(view)

        val user = DataStore.currentUser

        view.findViewById<TextView>(R.id.tvProfileName).text = user.name
        view.findViewById<TextView>(R.id.tvProfileEmail).text = user.email
        view.findViewById<TextView>(R.id.tvProfileJoined).text =
            getString(R.string.profile_joined_format, user.joinedDate)

        bindFinancialInsight(view)
    }

    private fun applyProfileInsets(view: View) {
        val titlePage = view.findViewById<View>(R.id.tvTitlePage)
        val initialTopMargin = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            titlePage.layoutParams = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = initialTopMargin + (statusBars.top / 2)
            }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun bindFinancialInsight(view: View) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        val monthKey = DataStore.getCurrentMonthKey()
        val transactions = DataStore.getAll()
        val monthlyIncome = DataStore.getIncomeByMonth(monthKey)
        val monthlyExpense = DataStore.getExpenseByMonth(monthKey)
        val monthlyLimit = DataStore.getMonthlyLimit(monthKey)
        val remainingBudget = monthlyLimit - monthlyExpense
        val budgetProgress = calculateBudgetProgress(monthlyExpense, monthlyLimit)
        val status = getBudgetStatus(budgetProgress, monthlyExpense, monthlyLimit)
        val monthlyHasActivity = monthlyIncome > 0 || monthlyExpense > 0

        val tvMonth = view.findViewById<TextView>(R.id.tvProfileMonth)
        val tvStatus = view.findViewById<TextView>(R.id.tvProfileBudgetStatus)
        val tvBudgetMain = view.findViewById<TextView>(R.id.tvProfileBudgetMain)
        val tvBudgetCaption = view.findViewById<TextView>(R.id.tvProfileBudgetCaption)
        val progressBudget = view.findViewById<ProgressBar>(R.id.progressProfileBudget)
        val tvMonthlyEmpty = view.findViewById<TextView>(R.id.tvProfileMonthlyEmpty)

        tvMonth.text = formatMonthYear(monthKey)
        tvStatus.text = getString(status.labelRes)
        tvStatus.setBackgroundResource(status.pillBackgroundRes)
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), status.textColorRes))

        tvBudgetMain.text = if (remainingBudget < 0) {
            getString(R.string.budget_over_limit_amount_format, formatter.format(-remainingBudget))
        } else {
            getString(R.string.budget_remaining_amount_format, formatter.format(remainingBudget))
        }
        tvBudgetCaption.text = getString(
            R.string.budget_usage_format,
            formatter.format(monthlyExpense),
            formatter.format(monthlyLimit)
        )
        progressBudget.max = BUDGET_PROGRESS_MAX
        progressBudget.progress = budgetProgress * BUDGET_PROGRESS_SCALE
        progressBudget.progressDrawable = ContextCompat.getDrawable(
            requireContext(),
            status.progressDrawableRes
        )
        progressBudget.contentDescription = getString(
            R.string.profile_budget_usage_desc,
            getString(status.labelRes),
            formatter.format(monthlyExpense),
            formatter.format(monthlyLimit),
            budgetProgress
        )
        tvMonthlyEmpty.visibility = if (monthlyHasActivity) View.GONE else View.VISIBLE

        view.findViewById<TextView>(R.id.tvTransactionCount).text =
            getString(R.string.profile_transaction_count_format, transactions.size)
        view.findViewById<TextView>(R.id.tvProfileTopCategory).text =
            getTopExpenseCategory(monthKey) ?: getString(R.string.profile_top_category_empty)
    }

    private fun calculateBudgetProgress(monthlyExpense: Int, monthlyLimit: Int): Int {
        if (monthlyLimit <= 0) return 0
        return ((monthlyExpense.toFloat() / monthlyLimit.toFloat()) * 100f)
            .toInt()
            .coerceIn(0, 100)
    }

    private fun getBudgetStatus(progress: Int, monthlyExpense: Int, monthlyLimit: Int): BudgetStatus {
        return when {
            monthlyLimit > 0 && monthlyExpense > monthlyLimit -> BudgetStatus(
                labelRes = R.string.budget_status_exceeded,
                pillBackgroundRes = R.drawable.bg_budget_status_exceeded,
                progressDrawableRes = R.drawable.bg_budget_progress_caution,
                textColorRes = R.color.pale_red
            )
            progress >= BUDGET_CAUTION_THRESHOLD -> BudgetStatus(
                labelRes = R.string.budget_status_caution,
                pillBackgroundRes = R.drawable.bg_budget_status_caution,
                progressDrawableRes = R.drawable.bg_budget_progress_limit,
                textColorRes = R.color.deep_teal
            )
            else -> BudgetStatus(
                labelRes = R.string.budget_status_safe,
                pillBackgroundRes = R.drawable.bg_budget_status_pill,
                progressDrawableRes = R.drawable.bg_budget_progress_safe,
                textColorRes = R.color.deep_teal
            )
        }
    }

    private fun getTopExpenseCategory(monthKey: String): String? {
        return DataStore.getAll()
            .asSequence()
            .filter { transaction ->
                transaction.type == "expense" && transaction.date.startsWith(monthKey)
            }
            .groupBy { it.category }
            .maxByOrNull { groupedCategory ->
                groupedCategory.value.sumOf { it.amount }
            }
            ?.key
    }

    private fun formatMonthYear(monthKey: String): String {
        return try {
            val parsedMonth = SimpleDateFormat("yyyy-MM", Locale.US).parse(monthKey)
            if (parsedMonth != null) {
                SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID")).format(parsedMonth)
            } else {
                monthKey
            }
        } catch (_: Exception) {
            monthKey
        }
    }

    private data class BudgetStatus(
        val labelRes: Int,
        val pillBackgroundRes: Int,
        val progressDrawableRes: Int,
        val textColorRes: Int
    )

    companion object {
        private const val BUDGET_CAUTION_THRESHOLD = 75
        private const val BUDGET_PROGRESS_SCALE = 10
        private const val BUDGET_PROGRESS_MAX = 100 * BUDGET_PROGRESS_SCALE
    }
}
