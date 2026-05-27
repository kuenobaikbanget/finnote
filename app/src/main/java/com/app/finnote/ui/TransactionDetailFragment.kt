package com.app.finnote.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionDetailFragment : Fragment() {

    companion object {
        private const val ARG_TRANSACTION_INDEX = "transaction_index"

        fun newInstance(index: Int): TransactionDetailFragment {
            val fragment = TransactionDetailFragment()
            val args = Bundle()
            args.putInt(ARG_TRANSACTION_INDEX, index)
            fragment.arguments = args
            return fragment
        }
    }

    private var transactionIndex: Int = -1
    private var transaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionIndex = arguments?.getInt(ARG_TRANSACTION_INDEX, -1) ?: -1
        applyTopBarInsets(view)

        if (transactionIndex >= 0 && transactionIndex < DataStore.transactions.size) {
            transaction = DataStore.transactions[transactionIndex]
            setupUI(view)
        } else {
            parentFragmentManager.popBackStack()
            return
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI(view: View) {
        val transaction = transaction ?: return
        val isIncome = transaction.type == "income"

        val currencyFormatter = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("id-ID")
        ).apply {
            maximumFractionDigits = 0
        }

        // Amount hero
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        tvAmount.text = currencyFormatter.format(transaction.amount)

        // Type icon rotation and tint
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val typeColor = if (isIncome) "#2f6f44".toColorInt() else "#ff6b6c".toColorInt()
        ivIcon.rotation = if (isIncome) 0f else 180f
        ivIcon.setColorFilter(typeColor)

        // Type labels
        val typeLabel = if (isIncome) {
            getString(R.string.home_income_label)
        } else {
            getString(R.string.home_expense_label)
        }
        view.findViewById<TextView>(R.id.tvType).apply {
            text = typeLabel
            setTextColor(typeColor)
        }
        view.findViewById<TextView>(R.id.tvTypeDetail).text = typeLabel

        // Category badge and detail row
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvCategoryDetail = view.findViewById<TextView>(R.id.tvCategoryDetail)
        if (transaction.category.isNotBlank()) {
            tvCategory.text = transaction.category
            tvCategory.backgroundTintList = ColorStateList.valueOf(typeColor)
            tvCategory.setTextColor("#f6fcf9".toColorInt())
            tvCategory.visibility = View.VISIBLE
            tvCategoryDetail.text = transaction.category
            tvCategoryDetail.setTextColor(typeColor)
        } else {
            tvCategory.backgroundTintList = null
            tvCategory.visibility = View.GONE
            tvCategoryDetail.text = "-"
            tvCategoryDetail.setTextColor("#0d1f2d".toColorInt())
        }

        // Detail rows
        view.findViewById<TextView>(R.id.tvTitle).text = transaction.title
        view.findViewById<TextView>(R.id.tvDate).text = formatDate(transaction.date)

        // Description section
        val descriptionSection = view.findViewById<LinearLayout>(R.id.descriptionSection)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        if (transaction.description.isNotBlank()) {
            tvDescription.text = transaction.description
            descriptionSection.visibility = View.VISIBLE
        } else {
            descriptionSection.visibility = View.GONE
        }
    }

    private fun applyTopBarInsets(view: View) {
        val headerContainer = view.findViewById<View>(R.id.headerContainer)
        val initialPaddingLeft = headerContainer.paddingLeft
        val initialPaddingTop = headerContainer.paddingTop
        val initialPaddingRight = headerContainer.paddingRight
        val initialPaddingBottom = headerContainer.paddingBottom
        val initialHeight = headerContainer.layoutParams.height

        ViewCompat.setOnApplyWindowInsetsListener(headerContainer) { header, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            header.setPadding(
                initialPaddingLeft,
                initialPaddingTop + statusBars.top,
                initialPaddingRight,
                initialPaddingBottom
            )
            if (initialHeight > 0) {
                header.layoutParams = header.layoutParams.apply {
                    height = initialHeight + statusBars.top
                }
            }
            insets
        }
        ViewCompat.requestApplyInsets(headerContainer)
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
}
