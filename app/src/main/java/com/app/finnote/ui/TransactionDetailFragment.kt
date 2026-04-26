package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
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

        if (transactionIndex >= 0 && transactionIndex < DataStore.transactions.size) {
            transaction = DataStore.transactions[transactionIndex]
            setupUI(view)
        } else {
            // Transaction not found, go back
            parentFragmentManager.popBackStack()
        }

        // Back button
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI(view: View) {
        val transaction = transaction ?: return

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }


        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        tvAmount.text = currencyFormatter.format(transaction.amount)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = transaction.title

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        tvDate.text = formatDate(transaction.date)

        val tvType = view.findViewById<TextView>(R.id.tvType)
        val tvTypeDetail = view.findViewById<TextView>(R.id.tvTypeDetail)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)

        val isIncome = transaction.type == "income"
        val typeLabel = if (isIncome) getString(R.string.home_income_label) else getString(R.string.home_expense_label)
        tvType.text = typeLabel
        tvTypeDetail.text = typeLabel

        // Set colors based on type
        val typeColor = if (isIncome) "#7dbe7e".toColorInt() else "#ff6b6c".toColorInt()
        tvType.setTextColor(typeColor)
        tvTypeDetail.setTextColor(typeColor)

        // Set icon rotation
        ivIcon.rotation = if (isIncome) 0f else 180f
        ivIcon.setColorFilter(typeColor)
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
