package com.app.finnote.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment() {
    private val expenseCategories = listOf(
        "Makan & Minum",
        "Transport",
        "Belanja",
        "Tagihan",
        "Kos/Sewa",
        "Pendidikan",
        "Hiburan",
        "Kesehatan",
        "Hadiah",
        "Lainnya"
    )
    private val incomeCategories = listOf(
        "Gaji",
        "Freelance",
        "Uang Saku",
        "Bonus",
        "Jual Barang",
        "Lainnya"
    )

    private var transactionType = TYPE_EXPENSE
    private var selectedCategory: String? = null
    private val selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))

    private lateinit var typeToggle: MaterialButtonToggleGroup
    private lateinit var btnExpense: MaterialButton
    private lateinit var btnIncome: MaterialButton
    private lateinit var tilAmount: TextInputLayout
    private lateinit var tilTitle: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var etAmount: EditText
    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var tvCategoryValue: TextView
    private lateinit var tvCategoryError: TextView
    private lateinit var amountDivider: View
    private lateinit var btnSave: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        applyInsets(view)
        setupInitialState()
        setupListeners(view)
        updateSaveState()
    }

    private fun bindViews(view: View) {
        typeToggle = view.findViewById(R.id.typeToggle)
        btnExpense = view.findViewById(R.id.btnTypeExpense)
        btnIncome = view.findViewById(R.id.btnTypeIncome)
        tilAmount = view.findViewById(R.id.tilAmount)
        tilTitle = view.findViewById(R.id.tilTitle)
        tilDate = view.findViewById(R.id.tilDate)
        etAmount = view.findViewById(R.id.etAmount)
        etTitle = view.findViewById(R.id.etTitle)
        etDate = view.findViewById(R.id.etDate)
        etDescription = view.findViewById(R.id.etDescription)
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory)
        tvCategoryValue = view.findViewById(R.id.tvCategoryValue)
        tvCategoryError = view.findViewById(R.id.tvCategoryError)
        amountDivider = view.findViewById(R.id.amountDivider)
        btnSave = view.findViewById(R.id.btnSaveTransaction)
    }

    private fun applyInsets(view: View) {
        val header = view.findViewById<View>(R.id.headerContainer)
        val content = view.findViewById<View>(R.id.addTransactionContent)
        val initialHeaderPaddingTop = header.paddingTop
        val initialHeaderHeight = header.layoutParams.height
        val initialContentPaddingBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            header.setPadding(
                header.paddingLeft,
                initialHeaderPaddingTop + statusBars.top,
                header.paddingRight,
                header.paddingBottom
            )
            if (initialHeaderHeight > 0) {
                header.layoutParams = header.layoutParams.apply {
                    height = initialHeaderHeight + statusBars.top
                }
            }
            content.setPadding(
                content.paddingLeft,
                content.paddingTop,
                content.paddingRight,
                initialContentPaddingBottom + maxOf(navBars.bottom, ime.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun setupInitialState() {
        typeToggle.check(R.id.btnTypeExpense)
        etDate.setText(formatDateLabel())
        renderCategoryChips(expenseCategories)
        updateTypeButtonColors()
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        typeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            transactionType = if (checkedId == R.id.btnTypeIncome) TYPE_INCOME else TYPE_EXPENSE
            selectedCategory = null
            tvCategoryValue.text = getString(R.string.add_transaction_category_placeholder)
            tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            tvCategoryError.visibility = View.GONE
            renderCategoryChips(if (transactionType == TYPE_INCOME) incomeCategories else expenseCategories)
            updateTypeButtonColors()
            updateSaveState()
        }

        etAmount.addTextChangedListener(formWatcher)
        etTitle.addTextChangedListener(formWatcher)
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateAmount(showError = true)
        }
        etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateTitle(showError = true)
        }
        etDate.setOnClickListener { showDatePicker() }
        tilDate.setEndIconOnClickListener { showDatePicker() }
        btnSave.setOnClickListener { saveTransaction() }
    }

    private fun renderCategoryChips(categories: List<String>) {
        chipGroupCategory.removeAllViews()
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                minHeight = resources.getDimensionPixelSize(R.dimen.add_transaction_chip_min_height)
                chipCornerRadius = resources.getDimension(R.dimen.add_transaction_chip_radius)
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_text))
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_bg)
                chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_stroke)
                chipStrokeWidth = resources.getDimension(R.dimen.add_transaction_chip_stroke)
                checkedIcon = null
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategory = category
                    tvCategoryValue.text = category
                    tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    tvCategoryError.visibility = View.GONE
                } else if (selectedCategory == category) {
                    selectedCategory = null
                    tvCategoryValue.text = getString(R.string.add_transaction_category_placeholder)
                    tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
                updateSaveState()
            }
            chipGroupCategory.addView(chip)
        }
    }

    private fun updateTypeButtonColors() {
        val expenseSelected = transactionType == TYPE_EXPENSE
        val incomeSelected = transactionType == TYPE_INCOME
        styleTypeButton(btnExpense, expenseSelected, R.color.pale_red)
        styleTypeButton(btnIncome, incomeSelected, R.color.chart_income_green)
        amountDivider.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (transactionType == TYPE_INCOME) R.color.chart_income_green else R.color.pale_red
            )
        )
    }

    private fun styleTypeButton(button: MaterialButton, isSelected: Boolean, colorRes: Int) {
        val context = requireContext()
        button.setTextColor(
            ContextCompat.getColor(
                context,
                if (isSelected) R.color.white else R.color.black
            )
        )
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(context, if (isSelected) colorRes else R.color.white)
        )
        button.strokeColor = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(context, if (isSelected) colorRes else R.color.divider_mist)
        )
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                etDate.setText(formatDateLabel())
                tilDate.error = null
                updateSaveState()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDateLabel(): String {
        val today = Calendar.getInstance()
        val isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        return if (isToday) getString(R.string.add_transaction_today) else displayDateFormat.format(selectedDate.time)
    }

    private fun saveTransaction() {
        val amount = parseAmount()
        val title = etTitle.text.toString().trim()
        val category = selectedCategory
        val isValid = validateAmount(showError = true) &&
            validateTitle(showError = true) &&
            validateCategory(showError = true) &&
            amount != null &&
            category != null

        if (!isValid) return

        DataStore.addTransaction(
            Transaction(
                title = title,
                amount = amount,
                date = dateFormat.format(selectedDate.time),
                type = transactionType,
                category = category,
                description = etDescription.text.toString().trim()
            )
        )
        parentFragmentManager.popBackStack()
        Snackbar.make(
            requireActivity().findViewById(R.id.fragmentContainer),
            R.string.add_transaction_success,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun updateSaveState() {
        btnSave.isEnabled = validateAmount(showError = false) &&
            validateTitle(showError = false) &&
            etDate.text.isNotBlank() &&
            selectedCategory != null
    }

    private fun validateAmount(showError: Boolean): Boolean {
        val amount = parseAmount()
        val isValid = amount != null && amount > 0
        if (showError) {
            tilAmount.error = if (isValid) null else getString(R.string.add_transaction_amount_error)
        }
        return isValid
    }

    private fun validateTitle(showError: Boolean): Boolean {
        val isValid = etTitle.text.toString().trim().isNotBlank()
        if (showError) {
            tilTitle.error = if (isValid) null else getString(R.string.add_transaction_title_error)
        }
        return isValid
    }

    private fun validateCategory(showError: Boolean): Boolean {
        val isValid = selectedCategory != null
        if (showError) {
            tvCategoryError.visibility = if (isValid) View.GONE else View.VISIBLE
        }
        return isValid
    }

    private fun parseAmount(): Int? {
        val digits = etAmount.text.toString().filter { it.isDigit() }
        return digits.toIntOrNull()
    }

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etAmount.hasFocus()) tilAmount.error = null
            if (etTitle.hasFocus()) tilTitle.error = null
            updateSaveState()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    companion object {
        private const val TYPE_EXPENSE = "expense"
        private const val TYPE_INCOME = "income"
    }
}
