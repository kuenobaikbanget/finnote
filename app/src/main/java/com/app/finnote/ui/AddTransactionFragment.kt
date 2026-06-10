package com.app.finnote.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.model.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment() {
    private val primaryExpenseCategories = listOf(
        "Makan & Minum",
        "Transport",
        "Belanja",
        "Tagihan",
        MORE_CATEGORY
    )
    private val secondaryExpenseCategories = listOf(
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
    private var isCategoryExpanded = false
    private var isDirty = false
    private var isRestoringDraft = false
    private var isFormattingAmount = false
    private var discardDialog: BottomSheetDialog? = null
    private val selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    private val amountFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    private val delightInterpolator = PathInterpolator(0.22f, 1f, 0.36f, 1f)

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
    private lateinit var dateRow: View
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var tvCategoryValue: TextView
    private lateinit var tvTitleError: TextView
    private lateinit var tvCategoryError: TextView
    private lateinit var amountDivider: View
    private lateinit var saveBar: View
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
        setupInitialState(savedInstanceState)
        setupListeners(view)
        restoreDraft(savedInstanceState)
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
        dateRow = view.findViewById(R.id.dateRow)
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory)
        tvCategoryValue = view.findViewById(R.id.tvCategoryValue)
        tvTitleError = view.findViewById(R.id.tvTitleError)
        tvCategoryError = view.findViewById(R.id.tvCategoryError)
        amountDivider = view.findViewById(R.id.amountDivider)
        saveBar = view.findViewById(R.id.saveBar)
        btnSave = view.findViewById(R.id.btnSaveTransaction)
    }

    private fun applyInsets(view: View) {
        val header = view.findViewById<View>(R.id.headerContainer)
        val content = view.findViewById<View>(R.id.addTransactionContent)
        val initialHeaderPaddingTop = header.paddingTop
        val initialHeaderHeight = header.layoutParams.height
        val initialContentPaddingBottom = content.paddingBottom
        val initialSaveBarPaddingBottom = saveBar.paddingBottom

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
                initialContentPaddingBottom
            )
            saveBar.setPadding(
                saveBar.paddingLeft,
                saveBar.paddingTop,
                saveBar.paddingRight,
                initialSaveBarPaddingBottom + maxOf(navBars.bottom, ime.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun setupInitialState(savedInstanceState: Bundle?) {
        typeToggle.check(R.id.btnTypeExpense)
        if (savedInstanceState == null) {
            etDate.setText(formatDateLabel())
        }
        updateDateA11yLabel()
        renderCategoryChips()
        updateTypeButtonColors()
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            handleBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )

        typeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newTransactionType = if (checkedId == R.id.btnTypeIncome) TYPE_INCOME else TYPE_EXPENSE
            transactionType = newTransactionType
            if (isRestoringDraft) {
                updateTypeButtonColors()
                return@addOnButtonCheckedListener
            }
            selectedCategory = null
            isCategoryExpanded = false
            markDirty()
            tvCategoryValue.text = getString(R.string.add_transaction_category_placeholder)
            tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted_accessible))
            tvCategoryError.visibility = View.GONE
            renderCategoryChips()
            updateTypeButtonColors()
            updateSaveState()
        }

        etAmount.addTextChangedListener(amountWatcher)
        etTitle.addTextChangedListener(formWatcher)
        etDescription.addTextChangedListener(formWatcher)
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateAmount(showError = true)
        }
        etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateTitle(showError = true)
        }
        etTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveTransaction()
                true
            } else {
                false
            }
        }
        dateRow.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener {
            playSavePressDelight()
            saveTransaction()
        }
    }

    fun getVisibleCategories(): List<String> {
        return when {
            transactionType == TYPE_INCOME -> incomeCategories
            isCategoryExpanded -> primaryExpenseCategories.filterNot { it == MORE_CATEGORY } + secondaryExpenseCategories
            else -> primaryExpenseCategories
        }
    }

    fun showMoreCategories() {
        isCategoryExpanded = true
        renderCategoryChips()
    }

    private fun renderCategoryChips() {
        chipGroupCategory.removeAllViews()
        getVisibleCategories().forEach { category ->
            val isMoreAction = transactionType == TYPE_EXPENSE && !isCategoryExpanded && category == MORE_CATEGORY
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = !isMoreAction
                minHeight = resources.getDimensionPixelSize(R.dimen.add_transaction_chip_min_height)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.add_transaction_chip_radius))
                    .build()
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_text))
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_bg)
                chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.add_transaction_chip_stroke)
                chipStrokeWidth = resources.getDimension(R.dimen.add_transaction_chip_stroke)
                checkedIcon = null
                isChecked = selectedCategory == category
            }
            updateCategoryChipA11y(chip, category, chip.isChecked, isMoreAction)
            if (isMoreAction) {
                chip.setOnClickListener {
                    showMoreCategories()
                }
                chipGroupCategory.addView(chip)
                return@forEach
            }
            chip.setOnCheckedChangeListener { currentChip, isChecked ->
                if (isChecked) {
                    selectedCategory = category
                    tvCategoryValue.text = category
                    tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    tvCategoryError.visibility = View.GONE
                    ViewCompat.setStateDescription(chipGroupCategory, null)
                    playCategorySelectedDelight(currentChip as Chip)
                    markDirty()
                } else if (selectedCategory == category) {
                    selectedCategory = null
                    tvCategoryValue.text = getString(R.string.add_transaction_category_placeholder)
                    tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted_accessible))
                    markDirty()
                }
                updateCategoryChipA11y(currentChip as Chip, category, isChecked, isMoreAction = false)
                updateSaveState()
            }
            chipGroupCategory.addView(chip)
        }
    }

    private fun updateTypeButtonColors() {
        val expenseSelected = transactionType == TYPE_EXPENSE
        val incomeSelected = transactionType == TYPE_INCOME
        styleTypeButton(
            btnExpense,
            expenseSelected,
            R.color.add_transaction_type_expense_bg,
            R.color.add_transaction_type_expense_stroke
        )
        styleTypeButton(
            btnIncome,
            incomeSelected,
            R.color.add_transaction_type_income_bg,
            R.color.add_transaction_type_income_stroke
        )
        updateTypeButtonA11y(btnExpense, getString(R.string.home_expense_label), expenseSelected)
        updateTypeButtonA11y(btnIncome, getString(R.string.home_income_label), incomeSelected)
        amountDivider.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (transactionType == TYPE_INCOME) R.color.chart_income_green else R.color.pale_red
            )
        )
    }

    private fun styleTypeButton(
        button: MaterialButton,
        isSelected: Boolean,
        backgroundSelectorRes: Int,
        strokeSelectorRes: Int
    ) {
        val context = requireContext()
        button.isSelected = isSelected
        button.setTextColor(ContextCompat.getColorStateList(context, R.color.add_transaction_type_text))
        button.backgroundTintList = ContextCompat.getColorStateList(context, backgroundSelectorRes)
        button.strokeColor = ContextCompat.getColorStateList(context, strokeSelectorRes)
    }

    private fun updateTypeButtonA11y(button: MaterialButton, label: String, isSelected: Boolean) {
        button.contentDescription = label
        ViewCompat.setStateDescription(
            button,
            getString(
                if (isSelected) R.string.add_transaction_state_selected
                else R.string.add_transaction_state_unselected
            )
        )
    }

    private fun updateCategoryChipA11y(
        chip: Chip,
        category: String,
        isChecked: Boolean,
        isMoreAction: Boolean
    ) {
        if (isMoreAction) {
            chip.contentDescription = getString(R.string.add_transaction_category_more_desc)
            ViewCompat.setStateDescription(chip, null)
            return
        }

        chip.contentDescription = category
        ViewCompat.setStateDescription(
            chip,
            getString(
                if (isChecked) R.string.add_transaction_state_selected
                else R.string.add_transaction_state_unselected
            )
        )
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                etDate.setText(formatDateLabel())
                updateDateA11yLabel()
                tilDate.error = null
                markDirty()
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

    private fun updateDateA11yLabel() {
        dateRow.contentDescription = getString(R.string.add_transaction_date_row_desc, etDate.text.toString())
    }

    private fun playSavePressDelight() {
        if (shouldReduceMotion()) return

        btnSave.animate()
            .scaleX(0.985f)
            .scaleY(0.985f)
            .setDuration(SAVE_PRESS_DOWN_DURATION_MS)
            .setInterpolator(delightInterpolator)
            .withEndAction {
                btnSave.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SAVE_PRESS_UP_DURATION_MS)
                    .setInterpolator(delightInterpolator)
                    .start()
            }
            .start()
    }

    private fun playCategorySelectedDelight(chip: Chip) {
        if (shouldReduceMotion()) return

        chip.animate().cancel()
        chip.scaleX = 0.98f
        chip.scaleY = 0.98f
        chip.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(CATEGORY_SELECT_DURATION_MS)
            .setInterpolator(delightInterpolator)
            .start()
    }

    private fun shouldReduceMotion(): Boolean {
        return try {
            Settings.Global.getFloat(
                requireContext().contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }

    private fun saveTransaction() {
        val amount = parseAmount()
        val title = etTitle.text.toString().trim()
        val category = selectedCategory
        if (!validateFormForSave(amount, category)) return

        btnSave.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

        DataStore.addTransaction(
            Transaction(
                title = title,
                amount = amount!!.toInt(),
                date = dateFormat.format(selectedDate.time),
                type = transactionType,
                category = category!!,
                description = etDescription.text.toString().trim()
            )
        )
        isDirty = false
        parentFragmentManager.popBackStack()
        Snackbar.make(
            requireActivity().findViewById(R.id.fragmentContainer),
            if (transactionType == TYPE_INCOME) {
                R.string.add_transaction_income_success
            } else {
                R.string.add_transaction_expense_success
            },
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun updateSaveState() {
        btnSave.isEnabled = validateAmount(showError = false) &&
            validateTitle(showError = false) &&
            etDate.text.isNotBlank() &&
            selectedCategory != null
    }

    private fun validateFormForSave(amount: Long?, category: String?): Boolean {
        val amountValid = validateAmount(showError = true)
        val titleValid = validateTitle(showError = true)
        val categoryValid = validateCategory(showError = true)
        val isValid = amountValid && titleValid && categoryValid && amount != null && category != null

        if (!isValid) {
            focusFirstInvalidField(amountValid, titleValid, categoryValid)
        }
        return isValid
    }

    private fun focusFirstInvalidField(
        amountValid: Boolean,
        titleValid: Boolean,
        categoryValid: Boolean
    ) {
        when {
            !amountValid -> etAmount.requestFocus()
            !titleValid -> etTitle.requestFocus()
            !categoryValid -> chipGroupCategory.requestFocus()
        }
    }

    private fun validateAmount(showError: Boolean): Boolean {
        val amount = parseAmount()
        val isValid = amount != null && amount in 1..MAX_AMOUNT
        if (showError) {
            setTextFieldError(tilAmount, when {
                isValid -> null
                amount != null && amount > MAX_AMOUNT -> {
                    getString(R.string.add_transaction_amount_too_large_error)
                }
                else -> getString(R.string.add_transaction_amount_error)
            })
        }
        return isValid
    }

    private fun validateTitle(showError: Boolean): Boolean {
        val isValid = etTitle.text.toString().trim().isNotBlank()
        if (showError) {
            tvTitleError.visibility = if (isValid) View.GONE else View.VISIBLE
            ViewCompat.setStateDescription(
                tilTitle,
                if (isValid) null else getString(R.string.add_transaction_title_error)
            )
        }
        return isValid
    }

    private fun setTextFieldError(layout: TextInputLayout, error: String?) {
        layout.error = error
        layout.isErrorEnabled = error != null
    }

    private fun validateCategory(showError: Boolean): Boolean {
        val isValid = selectedCategory != null
        if (showError) {
            tvCategoryError.visibility = if (isValid) View.GONE else View.VISIBLE
            val stateDescription = if (isValid) null else getString(R.string.add_transaction_category_error)
            ViewCompat.setStateDescription(chipGroupCategory, stateDescription)
            if (!isValid) {
                chipGroupCategory.requestFocus()
            }
        }
        return isValid
    }

    private fun parseAmount(): Long? {
        val digits = etAmount.text.toString().filter { it.isDigit() }
        return digits.toLongOrNull()
    }

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etTitle.hasFocus()) {
                tvTitleError.visibility = View.GONE
                ViewCompat.setStateDescription(tilTitle, null)
            }
            markDirty()
            updateSaveState()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    private val amountWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (isFormattingAmount) return
            if (etAmount.hasFocus()) tilAmount.error = null
            val raw = s?.toString().orEmpty()
            val digits = raw.filter { it.isDigit() }.trimStart('0')
            val formatted = digits.toLongOrNull()?.let { amountFormat.format(it) }.orEmpty()
            if (raw != formatted) {
                val cursorDigitCount = raw.take(etAmount.selectionStart.coerceAtLeast(0)).count { it.isDigit() }
                isFormattingAmount = true
                etAmount.setText(formatted)
                etAmount.setSelection(findCursorPosition(formatted, cursorDigitCount))
                isFormattingAmount = false
            }
            markDirty()
            updateSaveState()
        }
    }

    private fun findCursorPosition(formatted: String, digitCountBeforeCursor: Int): Int {
        if (digitCountBeforeCursor <= 0) return formatted.length
        var digitsSeen = 0
        formatted.forEachIndexed { index, char ->
            if (char.isDigit()) digitsSeen++
            if (digitsSeen >= digitCountBeforeCursor) return index + 1
        }
        return formatted.length
    }

    private fun markDirty() {
        if (!isRestoringDraft) {
            isDirty = true
        }
    }

    private fun handleBack() {
        if (!isDirty) {
            parentFragmentManager.popBackStack()
            return
        }

        discardDialog?.dismiss()

        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_discard_transaction, null)
        val sheetContent = sheetView.findViewById<View>(R.id.discardSheetContent)
        val initialPaddingBottom = sheetContent.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(sheetContent) { currentView, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            currentView.setPadding(
                currentView.paddingLeft,
                currentView.paddingTop,
                currentView.paddingRight,
                initialPaddingBottom + navBars.bottom
            )
            insets
        }

        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(sheetView)
            setCanceledOnTouchOutside(true)
            setOnDismissListener { discardDialog = null }
        }
        discardDialog = dialog

        sheetView.findViewById<MaterialButton>(R.id.btnDiscardKeepEditing).setOnClickListener {
            dialog.dismiss()
        }
        sheetView.findViewById<MaterialButton>(R.id.btnDiscardConfirm).setOnClickListener {
            isDirty = false
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                (parent as? View)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        ViewCompat.requestApplyInsets(sheetContent)
    }

    private fun restoreDraft(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return

        isRestoringDraft = true
        transactionType = savedInstanceState.getString(KEY_TYPE, TYPE_EXPENSE)
        selectedCategory = savedInstanceState.getString(KEY_CATEGORY)
        isCategoryExpanded = savedInstanceState.getBoolean(KEY_CATEGORY_EXPANDED, false)
        isDirty = savedInstanceState.getBoolean(KEY_DIRTY, false)
        selectedDate.timeInMillis = savedInstanceState.getLong(KEY_DATE, selectedDate.timeInMillis)

        typeToggle.check(if (transactionType == TYPE_INCOME) R.id.btnTypeIncome else R.id.btnTypeExpense)
        etAmount.setText(savedInstanceState.getString(KEY_AMOUNT).orEmpty())
        etTitle.setText(savedInstanceState.getString(KEY_TITLE).orEmpty())
        etDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION).orEmpty())
        etDate.setText(formatDateLabel())
        updateDateA11yLabel()
        selectedCategory?.let {
            tvCategoryValue.text = it
            tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } ?: run {
            tvCategoryValue.text = getString(R.string.add_transaction_category_placeholder)
            tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted_accessible))
        }
        renderCategoryChips()
        updateTypeButtonColors()
        isRestoringDraft = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TYPE, transactionType)
        outState.putString(KEY_CATEGORY, selectedCategory)
        outState.putBoolean(KEY_CATEGORY_EXPANDED, isCategoryExpanded)
        outState.putBoolean(KEY_DIRTY, isDirty)
        outState.putLong(KEY_DATE, selectedDate.timeInMillis)
        outState.putString(KEY_AMOUNT, etAmount.text.toString())
        outState.putString(KEY_TITLE, etTitle.text.toString())
        outState.putString(KEY_DESCRIPTION, etDescription.text.toString())
    }

    companion object {
        private const val MAX_AMOUNT = 999_999_999L
        private const val SAVE_PRESS_DOWN_DURATION_MS = 70L
        private const val SAVE_PRESS_UP_DURATION_MS = 140L
        private const val CATEGORY_SELECT_DURATION_MS = 160L
        private const val MORE_CATEGORY = "Lainnya"
        private const val TYPE_EXPENSE = "expense"
        private const val TYPE_INCOME = "income"
        private const val KEY_TYPE = "add_transaction_type"
        private const val KEY_CATEGORY = "add_transaction_category"
        private const val KEY_CATEGORY_EXPANDED = "add_transaction_category_expanded"
        private const val KEY_DIRTY = "add_transaction_dirty"
        private const val KEY_DATE = "add_transaction_date"
        private const val KEY_AMOUNT = "add_transaction_amount"
        private const val KEY_TITLE = "add_transaction_title"
        private const val KEY_DESCRIPTION = "add_transaction_description"
    }
}
