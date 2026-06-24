package com.app.finnote.ui

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.app.finnote.MainActivity
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.component.RecentTransactionsView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var recentView: RecentTransactionsView? = null
    private var isBudgetLimitMode = false
    private var hasPlayedHomeDataAnimation = false
    private var hasBoundHomeData = false
    private var budgetProgressAnimator: ValueAnimator? = null
    private val cashflowInterpolator = PathInterpolator(0.22f, 1f, 0.36f, 1f)
    private val budgetProgressInterpolator = PathInterpolator(0.22f, 1f, 0.36f, 1f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val ivHomeProfile = view.findViewById<ImageView>(R.id.ivHomeProfile)
        applyHomeInsets(view)
        setupStickyHeaderMotion(view)
        recentView = view.findViewById(R.id.containerRecent)
        recentView?.setFragmentManager(parentFragmentManager, this)
        ivHomeProfile.setOnClickListener {
            (requireActivity() as? MainActivity)?.openProfile()
        }
        view.findViewById<View>(R.id.fabAddTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TransactionFormFragment())
                .addToBackStack(null)
                .commit()
        }

        val user = DataStore.getCurrentUser()
        val firstName = user.name.split(" ")[0]
        tvWelcome.text = if (DataStore.isLoggedIn()) {
            if (firstName.isBlank()) {
                getString(R.string.welcome_guest)
            } else {
                getString(R.string.welcome_user, firstName)
            }
        } else {
            "User"
        }
        
        if (!DataStore.isLoggedIn()) {
            ivHomeProfile.setImageResource(R.drawable.ic_photo_profile_round)
        }

        bindHomeData(view)
    }

    private fun applyHomeInsets(view: View) {
        val homeContent = view.findViewById<View>(R.id.homeContent)
        val layoutLogo = view.findViewById<View>(R.id.layoutLogo)
        val initialPaddingLeft = homeContent.paddingLeft
        val initialPaddingTop = homeContent.paddingTop
        val initialPaddingRight = homeContent.paddingRight
        val initialPaddingBottom = homeContent.paddingBottom
        val headerInitialPaddingLeft = layoutLogo.paddingLeft
        val headerInitialPaddingTop = layoutLogo.paddingTop
        val headerInitialPaddingRight = layoutLogo.paddingRight
        val headerInitialPaddingBottom = layoutLogo.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(layoutLogo) { header, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            header.setPadding(
                headerInitialPaddingLeft,
                headerInitialPaddingTop + statusBars.top,
                headerInitialPaddingRight,
                headerInitialPaddingBottom
            )
            header.layoutParams = header.layoutParams.apply {
                height = STICKY_HEADER_HEIGHT_DP.dpToPx() + statusBars.top
            }
            homeContent.setPadding(
                initialPaddingLeft,
                initialPaddingTop + statusBars.top,
                initialPaddingRight,
                initialPaddingBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(layoutLogo)
    }

    private fun setupStickyHeaderMotion(view: View) {
        val homeScrollView = view.findViewById<NestedScrollView>(R.id.homeScrollView)
        val layoutLogo = view.findViewById<View>(R.id.layoutLogo)
        val ivHomeLogo = view.findViewById<ImageView>(R.id.ivHomeLogo)
        val tvHomeLogoTitle = view.findViewById<TextView>(R.id.tvHomeLogoTitle)
        val ivHomeProfile = view.findViewById<ImageView>(R.id.ivHomeProfile)
        val topMenuSeparator = view.findViewById<View>(R.id.topMenuSeparator)

        homeScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val progress = (scrollY / STICKY_HEADER_COMPACT_SCROLL_RANGE_DP.dpToPx().toFloat())
                .coerceIn(0f, 1f)
            val separatorProgress = (scrollY / TOP_MENU_SEPARATOR_FADE_RANGE_DP.dpToPx().toFloat())
                .coerceIn(0f, 1f)
            bindStickyHeaderProgress(
                progress = progress,
                separatorProgress = separatorProgress,
                layoutLogo = layoutLogo,
                topMenuSeparator = topMenuSeparator,
                ivHomeLogo = ivHomeLogo,
                tvHomeLogoTitle = tvHomeLogoTitle,
                ivHomeProfile = ivHomeProfile
            )
        }
    }

    private fun bindStickyHeaderProgress(
        progress: Float,
        separatorProgress: Float,
        layoutLogo: View,
        topMenuSeparator: View,
        ivHomeLogo: ImageView,
        tvHomeLogoTitle: TextView,
        ivHomeProfile: ImageView
    ) {
        val easedProgress = 1f - ((1f - progress) * (1f - progress))
        val logoScale = lerp(1f, 0.84f, easedProgress)
        val titleScale = lerp(1f, 0.9f, easedProgress)
        val actionScale = lerp(1f, 0.88f, easedProgress)

        topMenuSeparator.alpha = separatorProgress
        ivHomeLogo.scaleX = logoScale
        ivHomeLogo.scaleY = logoScale
        tvHomeLogoTitle.scaleX = titleScale
        tvHomeLogoTitle.scaleY = titleScale
        tvHomeLogoTitle.translationX = lerp(0f, -4.dpToPx().toFloat(), easedProgress)
        ivHomeProfile.scaleX = actionScale
        ivHomeProfile.scaleY = actionScale
    }

    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + ((end - start) * progress)
    }

    private fun bindHomeData(view: View) {
        val tvIncome = view.findViewById<TextView>(R.id.tvIncome)
        val tvExpense = view.findViewById<TextView>(R.id.tvExpense)
        val tvMonthlyActivityTitle = view.findViewById<TextView>(R.id.tvMonthlyActivityTitle)
        val tvBudgetUsage = view.findViewById<TextView>(R.id.tvBudgetUsage)
        val tvBudgetRemaining = view.findViewById<TextView>(R.id.tvBudgetRemaining)
        val tvBudgetRemainingAmount = view.findViewById<TextView>(R.id.tvBudgetRemainingAmount)
        val tvBudgetEdit = view.findViewById<TextView>(R.id.tvBudgetEdit)
        val barIncomeComparison = view.findViewById<View>(R.id.barIncomeComparison)
        val barExpenseComparison = view.findViewById<View>(R.id.barExpenseComparison)
        val cardBudget = view.findViewById<View>(R.id.cardBudget)
        val progressBudget = view.findViewById<ProgressBar>(R.id.progressBudget)
        progressBudget.max = BUDGET_PROGRESS_MAX
        val monthKey = DataStore.getCurrentMonthKey()
        val monthlyLimit = DataStore.getMonthlyLimit(monthKey)
        val monthlyIncome = DataStore.getIncomeByMonth(monthKey)
        val monthlyExpense = DataStore.getExpenseByMonth(monthKey)
        val remainingBudget = (monthlyLimit - monthlyExpense).coerceAtLeast(0)
        val budgetProgress = if (monthlyLimit <= 0) {
            0
        } else {
            ((monthlyExpense.toFloat() / monthlyLimit.toFloat()) * 100f).toInt().coerceIn(0, 100)
        }
        val comparisonMax = maxOf(monthlyIncome, monthlyExpense, 1)
        val animateHomeData = shouldAnimateHomeData()

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

        tvIncome.text = getString(R.string.summary_income, currencyFormatter.format(monthlyIncome))
        tvExpense.text = getString(R.string.summary_expense, currencyFormatter.format(monthlyExpense))
        tvMonthlyActivityTitle.text = getString(R.string.monthly_expense_title, formatMonthYear(monthKey))
        tvBudgetRemainingAmount.text = if (monthlyExpense > monthlyLimit) {
            getString(R.string.budget_over_limit_amount_format, currencyFormatter.format(monthlyExpense - monthlyLimit))
        } else {
            getString(R.string.budget_remaining_amount_format, currencyFormatter.format(remainingBudget))
        }
        bindBudgetMode(
            tvBudgetUsage = tvBudgetUsage,
            tvBudgetRemaining = tvBudgetRemaining,
            progressBudget = progressBudget,
            monthlyExpenseText = currencyFormatter.format(monthlyExpense),
            monthlyLimitText = currencyFormatter.format(monthlyLimit),
            budgetProgress = budgetProgress,
            animate = animateHomeData,
            animateFromZero = animateHomeData
        )
        tvBudgetEdit.setOnClickListener {
            showEditBudgetLimitDialog(monthKey, monthlyLimit)
        }
        cardBudget.setOnClickListener {
            isBudgetLimitMode = !isBudgetLimitMode
            bindBudgetMode(
                tvBudgetUsage = tvBudgetUsage,
                tvBudgetRemaining = tvBudgetRemaining,
                progressBudget = progressBudget,
                monthlyExpenseText = currencyFormatter.format(monthlyExpense),
                monthlyLimitText = currencyFormatter.format(monthlyLimit),
                budgetProgress = budgetProgress,
                animate = areSystemAnimatorsEnabled(),
                animateFromZero = false
            )
        }
        bindCashflowBar(
            barIncomeComparison,
            monthlyIncome,
            comparisonMax,
            animateHomeData,
            CASHFLOW_INCOME_START_DELAY_MS
        )
        bindCashflowBar(
            barExpenseComparison,
            monthlyExpense,
            comparisonMax,
            animateHomeData,
            CASHFLOW_EXPENSE_START_DELAY_MS
        )
        barIncomeComparison.contentDescription = getCashflowBarDescription(
            label = getString(R.string.home_income_label),
            amountText = currencyFormatter.format(monthlyIncome),
            amount = monthlyIncome,
            maxAmount = comparisonMax
        )
        barExpenseComparison.contentDescription = getCashflowBarDescription(
            label = getString(R.string.home_expense_label),
            amountText = currencyFormatter.format(monthlyExpense),
            amount = monthlyExpense,
            maxAmount = comparisonMax
        )
        progressBudget.contentDescription = getString(R.string.budget_usage_format, currencyFormatter.format(monthlyExpense), currencyFormatter.format(monthlyLimit))
        recentView?.bindData(DataStore.getAll())
        hasPlayedHomeDataAnimation = true
    }

    private fun showEditBudgetLimitDialog(monthKey: String, currentLimit: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_budget_limit, null)
        val input = dialogView.findViewById<EditText>(R.id.etBudgetLimit)
        val errorText = dialogView.findViewById<TextView>(R.id.tvBudgetLimitError)
        val checkIcon = dialogView.findViewById<ImageView>(R.id.ivBudgetLimitCheck)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBudgetCancel)
        val btnSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBudgetSave)

        fun rawValue(): String = (input.text?.toString() ?: "").filter { it.isDigit() }

        fun formatWithDots(raw: String): String =
            if (raw.isEmpty()) "" else raw.reversed().chunked(3).joinToString(".").reversed()

        // Pre-populate with formatted value
        val currentLimitText = if (currentLimit > 0) formatWithDots(currentLimit.toString()) else ""
        input.setText(currentLimitText)
        input.setSelection(input.text?.length ?: 0)

        fun showError(message: String) {
            val wasVisible = errorText.visibility == View.VISIBLE
            errorText.text = message
            if (!wasVisible) {
                errorText.visibility = View.VISIBLE
                errorText.alpha = 0f
                errorText.animate()
                    .alpha(1f)
                    .setDuration(180L)
                    .setInterpolator(PathInterpolator(0.22f, 1f, 0.36f, 1f))
                    .start()
            }
            input.requestFocus()
        }

        fun clearError() {
            if (errorText.visibility == View.VISIBLE) {
                errorText.animate()
                    .alpha(0f)
                    .setDuration(140L)
                    .setInterpolator(PathInterpolator(0.22f, 1f, 0.36f, 1f))
                    .withEndAction {
                        errorText.visibility = View.GONE
                    }
                    .start()
            }
        }

        fun updateCheckIcon() {
            val value = rawValue().toLongOrNull()
            val showCheck = value != null && value > 0L && value <= Int.MAX_VALUE
            if (showCheck && checkIcon?.visibility != View.VISIBLE) {
                checkIcon?.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .setDuration(200L)
                        .setInterpolator(PathInterpolator(0.22f, 1f, 0.36f, 1f))
                        .start()
                }
            } else if (!showCheck && checkIcon?.visibility == View.VISIBLE) {
                checkIcon?.visibility = View.GONE
            }
        }

        // TextWatcher: thousand-separator dots + check icon + clear error
        input.addTextChangedListener(object : android.text.TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isFormatting) clearError()
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                if (isFormatting) return
                isFormatting = true

                val raw = (s?.toString() ?: "").filter { it.isDigit() }
                val formatted = formatWithDots(raw)

                if (formatted != s.toString()) {
                    val oldCursor = input.selectionEnd.coerceAtMost(s?.length ?: 0)
                    val digitsBeforeCursor = (s?.toString()?.substring(0, oldCursor) ?: "").count { it.isDigit() }

                    input.setText(formatted)

                    var newCursor = 0
                    var digitsSeen = 0
                    for (ch in formatted) {
                        if (digitsSeen >= digitsBeforeCursor) break
                        if (ch.isDigit()) digitsSeen++
                        newCursor++
                    }
                    input.setSelection(newCursor.coerceIn(0, formatted.length))
                }

                isFormatting = false
                updateCheckIcon()
            }
        })

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.budget_edit_title)
            .setView(dialogView)
            .show()

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog_budget_limit)
        )

        fun saveLimit() {
            val raw = rawValue()
            val limit = raw.toLongOrNull()
            when {
                limit == null || limit <= 0L -> {
                    showError(getString(R.string.budget_edit_empty_error))
                    return
                }
                limit > Int.MAX_VALUE -> {
                    showError(getString(R.string.budget_edit_too_large_error))
                    return
                }
            }

            clearError()
            DataStore.setMonthlyLimit(monthKey, limit.toInt())
            Toast.makeText(requireContext(), R.string.budget_edit_success, Toast.LENGTH_SHORT).show()
            view?.let { bindHomeData(it) }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener { saveLimit() }

        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                input.selectAll()
                clearError()
                updateCheckIcon()
            }
        }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveLimit()
                true
            } else {
                false
            }
        }
        input.post {
            input.requestFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as? android.view.inputmethod.InputMethodManager
            imm?.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun getCashflowBarDescription(
        label: String,
        amountText: String,
        amount: Int,
        maxAmount: Int
    ): String {
        val ratio = if (maxAmount <= 0) 0 else ((amount.toFloat() / maxAmount.toFloat()) * 100f).toInt()
        return getString(R.string.cashflow_bar_desc, label, amountText, ratio.coerceIn(0, 100))
    }

    private fun bindBudgetMode(
        tvBudgetUsage: TextView,
        tvBudgetRemaining: TextView,
        progressBudget: ProgressBar,
        monthlyExpenseText: String,
        monthlyLimitText: String,
        budgetProgress: Int,
        animate: Boolean,
        animateFromZero: Boolean
    ) {
        val progressDrawableRes = if (isBudgetLimitMode) {
            R.drawable.bg_budget_progress_limit
        } else {
            R.drawable.bg_budget_progress_safe
        }

        tvBudgetRemaining.text = getString(R.string.budget_remaining_caption)
        tvBudgetUsage.text = if (isBudgetLimitMode) {
            getString(R.string.budget_limit_label, monthlyLimitText)
        } else {
            getString(R.string.budget_used_label, monthlyExpenseText)
        }
        progressBudget.progressDrawable = ContextCompat.getDrawable(requireContext(), progressDrawableRes)
        animateBudgetProgress(
            progressBudget = progressBudget,
            targetProgress = if (isBudgetLimitMode) 0 else budgetProgress,
            animate = animate,
            animateFromZero = animateFromZero
        )
    }

    private fun bindCashflowBar(
        bar: View,
        amount: Int,
        maxAmount: Int,
        animate: Boolean,
        startDelay: Long
    ) {
        val maxHeight = 148.dpToPx()
        val minVisibleHeight = 12.dpToPx()
        val targetHeight = if (amount <= 0) {
            0
        } else {
            ((amount.toFloat() / maxAmount.toFloat()) * maxHeight)
                .toInt()
                .coerceIn(minVisibleHeight, maxHeight)
        }

        bar.layoutParams = bar.layoutParams.apply {
            height = targetHeight
        }
        bar.animate().cancel()

        if (targetHeight == 0) {
            bar.scaleY = 0f
            bar.alpha = 0f
            return
        }

        if (!animate) {
            bar.scaleY = 1f
            bar.alpha = 1f
            return
        }

        bar.scaleY = 0f
        bar.alpha = 0.72f
        bar.post {
            bar.pivotY = bar.height.toFloat()
            bar.animate()
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(startDelay)
                .setDuration(CASHFLOW_ANIMATION_DURATION_MS)
                .setInterpolator(cashflowInterpolator)
                .start()
        }
    }

    private fun animateBudgetProgress(
        progressBudget: ProgressBar,
        targetProgress: Int,
        animate: Boolean,
        animateFromZero: Boolean
    ) {
        val targetScaledProgress = targetProgress * BUDGET_PROGRESS_SCALE
        val startProgress = if (animateFromZero) 0 else progressBudget.progress
        budgetProgressAnimator?.cancel()
        if (!animate || startProgress == targetScaledProgress) {
            progressBudget.progress = targetScaledProgress
            return
        }

        progressBudget.progress = startProgress
        budgetProgressAnimator = ValueAnimator.ofInt(startProgress, targetScaledProgress).apply {
            duration = BUDGET_ANIMATION_DURATION_MS
            interpolator = budgetProgressInterpolator
            addUpdateListener { animator ->
                progressBudget.progress = animator.animatedValue as Int
            }
            start()
        }
    }

    private fun shouldAnimateHomeData(): Boolean {
        return !hasPlayedHomeDataAnimation && areSystemAnimatorsEnabled()
    }

    private fun areSystemAnimatorsEnabled(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()
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

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        if (hasBoundHomeData) {
            view?.let { bindHomeData(it) }
        } else {
            hasBoundHomeData = true
        }
    }

    override fun onDestroyView() {
        budgetProgressAnimator?.cancel()
        budgetProgressAnimator = null
        super.onDestroyView()
    }

    companion object {
        private const val CASHFLOW_ANIMATION_DURATION_MS = 420L
        private const val CASHFLOW_INCOME_START_DELAY_MS = 160L
        private const val CASHFLOW_EXPENSE_START_DELAY_MS = 280L
        private const val BUDGET_ANIMATION_DURATION_MS = 460L
        private const val BUDGET_PROGRESS_SCALE = 10
        private const val BUDGET_PROGRESS_MAX = 100 * BUDGET_PROGRESS_SCALE
        private const val STICKY_HEADER_HEIGHT_DP = 56
        private const val STICKY_HEADER_COMPACT_SCROLL_RANGE_DP = 96
        private const val TOP_MENU_SEPARATOR_FADE_RANGE_DP = 12
    }

}
