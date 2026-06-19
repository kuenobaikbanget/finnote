package com.app.finnote.ui

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.component.LedgerStampView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionSuccessFragment : Fragment() {
    private val entranceInterpolator = PathInterpolator(0.22f, 1f, 0.36f, 1f)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    private var transactionId: Int = -1
    private var amountAnimationData: AmountAnimationData? = null
    private var stampAnimator: ValueAnimator? = null
    private var amountAnimator: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_transaction_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionId = arguments?.getInt(ARG_TRANSACTION_ID, -1) ?: -1

        applyInsets(view)
        bindSuccessState(view)
        setupActions(view)
        setupBackHandling()
        playEntranceAnimation(view)
    }

    private fun applyInsets(view: View) {
        val content = view.findViewById<View>(R.id.transactionSuccessContent)
        val initialPaddingLeft = content.paddingLeft
        val initialPaddingTop = content.paddingTop
        val initialPaddingRight = content.paddingRight
        val initialPaddingBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            content.setPadding(
                initialPaddingLeft,
                initialPaddingTop + statusBars.top,
                initialPaddingRight,
                initialPaddingBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun bindSuccessState(view: View) {
        val transaction = DataStore.getTransactionById(transactionId)
        if (transaction == null) {
            bindFallbackState(view)
            return
        }

        val isIncome = transaction.type == TYPE_INCOME
        val typeLabel = getString(if (isIncome) R.string.home_income_label else R.string.home_expense_label)
        val typeColor = ContextCompat.getColor(
            requireContext(),
            if (isIncome) R.color.chart_income_green else R.color.expense_coral_accessible
        )
        val amountSign = if (isIncome) "+" else "-"
        amountAnimationData = AmountAnimationData(amountSign, transaction.amount)
        val amountText = formatSignedAmount(amountSign, transaction.amount)
        val categoryText = transaction.category.ifBlank { getString(R.string.transaction_success_empty_value) }
        val titleText = transaction.title.ifBlank { getString(R.string.transaction_success_empty_value) }
        val dateText = formatDate(transaction.date)

        view.findViewById<TextView>(R.id.tvSuccessAmount).apply {
            text = amountText
            setTextColor(typeColor)
        }
        view.findViewById<TextView>(R.id.tvSuccessType).apply {
            text = typeLabel
            setTextColor(typeColor)
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (isIncome) R.color.income_tint else R.color.expense_tint
                )
            )
        }
        view.findViewById<TextView>(R.id.tvSuccessTitleValue).text = titleText
        view.findViewById<TextView>(R.id.tvSuccessDateValue).text = dateText
        view.findViewById<TextView>(R.id.tvSuccessCategoryValue).text = categoryText

        view.findViewById<View>(R.id.successReceiptCard).contentDescription = getString(
            R.string.transaction_success_summary_desc,
            typeLabel,
            amountText,
            titleText,
            categoryText,
            dateText
        )
    }

    private fun bindFallbackState(view: View) {
        amountAnimationData = null
        view.findViewById<TextView>(R.id.tvSuccessMessage).text =
            getString(R.string.transaction_success_fallback_message)
        view.findViewById<View>(R.id.successReceiptCard).visibility = View.GONE
        view.findViewById<MaterialButton>(R.id.btnViewTransactionDetail).text =
            getString(R.string.transaction_success_view_transactions)
    }

    private fun setupActions(view: View) {
        view.findViewById<MaterialButton>(R.id.btnViewTransactionDetail).setOnClickListener {
            if (DataStore.getTransactionById(transactionId) != null) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, TransactionDetailFragment.newInstance(transactionId))
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, TransactionFragment())
                    .commit()
            }
        }

        view.findViewById<MaterialButton>(R.id.btnAddAnotherTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddTransactionFragment())
                .commit()
        }
    }

    private fun setupBackHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToPreviousSurface()
                }
            }
        )
    }

    private fun returnToPreviousSurface() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    private fun playEntranceAnimation(view: View) {
        val iconWrap = view.findViewById<View>(R.id.successIconWrap)
        val stamp = view.findViewById<LedgerStampView>(R.id.ledgerStampView)
        val copy = view.findViewById<View>(R.id.successCopyGroup)
        val receipt = view.findViewById<View>(R.id.successReceiptCard)
        val actions = view.findViewById<View>(R.id.transactionSuccessActions)
        val amount = view.findViewById<TextView>(R.id.tvSuccessAmount)
        val detailRows = listOf(
            view.findViewById<TextView>(R.id.tvSuccessTitleValue).parent as View,
            view.findViewById<TextView>(R.id.tvSuccessCategoryValue).parent as View,
            view.findViewById<TextView>(R.id.tvSuccessDateValue).parent as View
        )

        stampAnimator?.cancel()
        amountAnimator?.cancel()

        if (!areSystemAnimatorsEnabled()) {
            stamp.setStampProgress(1f)
            listOf(iconWrap, copy, receipt, actions).plus(detailRows).forEach { animatedView ->
                animatedView.alpha = 1f
                animatedView.translationY = 0f
                animatedView.scaleX = 1f
                animatedView.scaleY = 1f
            }
            return
        }

        amountAnimationData?.let { amount.text = formatSignedAmount(it.sign, 0) }
        stamp.setStampProgress(0f)
        prepareForReveal(iconWrap, translationY = 0f, scale = 0.94f)
        prepareForReveal(copy, translationY = 8.dpToPx())
        prepareForReveal(receipt, translationY = 20.dpToPx(), scale = 0.985f)
        detailRows.forEach { row -> prepareForReveal(row, translationY = 6.dpToPx()) }
        prepareForReveal(actions, translationY = 14.dpToPx())

        stampAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            startDelay = STAMP_START_DELAY_MS
            duration = STAMP_ANIMATION_DURATION_MS
            interpolator = entranceInterpolator
            addUpdateListener { animator ->
                stamp.setStampProgress(animator.animatedValue as Float)
            }
            start()
        }

        reveal(iconWrap, startDelay = 0L, duration = 240L)
        reveal(copy, startDelay = COPY_REVEAL_DELAY_MS, duration = 240L)
        reveal(receipt, startDelay = RECEIPT_REVEAL_DELAY_MS, duration = 340L)
        detailRows.forEachIndexed { index, row ->
            reveal(
                row,
                startDelay = RECEIPT_ROW_REVEAL_DELAY_MS + (index * RECEIPT_ROW_STAGGER_MS),
                duration = 220L
            )
        }
        reveal(actions, startDelay = ACTIONS_REVEAL_DELAY_MS, duration = 260L)
        animateAmount(amount)
    }

    private fun prepareForReveal(view: View, translationY: Float, scale: Float = 1f) {
        view.animate().cancel()
        view.alpha = 0f
        view.translationY = translationY
        view.scaleX = scale
        view.scaleY = scale
    }

    private fun reveal(view: View, startDelay: Long, duration: Long) {
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(startDelay)
            .setDuration(duration)
            .setInterpolator(entranceInterpolator)
            .start()
    }

    private fun animateAmount(amountView: TextView) {
        val amountData = amountAnimationData ?: return
        amountAnimator = ValueAnimator.ofInt(0, amountData.amount).apply {
            startDelay = AMOUNT_COUNT_DELAY_MS
            duration = AMOUNT_COUNT_DURATION_MS
            interpolator = entranceInterpolator
            addUpdateListener { animator ->
                amountView.text = formatSignedAmount(amountData.sign, animator.animatedValue as Int)
            }
            start()
        }
    }

    private fun formatSignedAmount(sign: String, amount: Int): String {
        return getString(R.string.transaction_amount_format, sign, currencyFormatter.format(amount))
    }

    private fun areSystemAnimatorsEnabled(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val parsedDate = inputFormat.parse(date)
            if (parsedDate != null) outputFormat.format(parsedDate) else date
        } catch (_: Exception) {
            date
        }
    }

    private fun Int.dpToPx(): Float {
        return this * resources.displayMetrics.density
    }

    override fun onDestroyView() {
        stampAnimator?.cancel()
        amountAnimator?.cancel()
        stampAnimator = null
        amountAnimator = null
        super.onDestroyView()
    }

    private data class AmountAnimationData(
        val sign: String,
        val amount: Int
    )

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"
        private const val TYPE_INCOME = "income"
        private const val STAMP_START_DELAY_MS = 180L
        private const val STAMP_ANIMATION_DURATION_MS = 560L
        private const val COPY_REVEAL_DELAY_MS = 320L
        private const val RECEIPT_REVEAL_DELAY_MS = 620L
        private const val RECEIPT_ROW_REVEAL_DELAY_MS = 780L
        private const val RECEIPT_ROW_STAGGER_MS = 90L
        private const val AMOUNT_COUNT_DELAY_MS = 720L
        private const val AMOUNT_COUNT_DURATION_MS = 520L
        private const val ACTIONS_REVEAL_DELAY_MS = 1080L

        fun newInstance(id: Int): TransactionSuccessFragment {
            return TransactionSuccessFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TRANSACTION_ID, id)
                }
            }
        }
    }
}
