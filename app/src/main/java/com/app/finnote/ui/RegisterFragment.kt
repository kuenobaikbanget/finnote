package com.app.finnote.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPasswordConfirm: TextInputLayout
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvRegisterError: TextView
    private lateinit var tvLogin: MaterialButton

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilName = view.findViewById(R.id.tilName)
        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        tilPasswordConfirm = view.findViewById(R.id.tilPasswordConfirm)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvRegisterError = view.findViewById(R.id.tvRegisterError)
        tvLogin = view.findViewById(R.id.tvLogin)

        val etName = tilName.editText!!
        val etEmail = tilEmail.editText!!
        val etPassword = tilPassword.editText!!
        val etPasswordConfirm = tilPasswordConfirm.editText!!

        val clearErrorOnInput = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tvRegisterError.visibility == View.VISIBLE) {
                    hideError()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etName.addTextChangedListener(clearErrorOnInput)
        etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etEmail.requestFocus()
                true
            } else false
        }

        etEmail.addTextChangedListener(clearErrorOnInput)
        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etPassword.requestFocus()
                true
            } else false
        }

        etPassword.addTextChangedListener(clearErrorOnInput)
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etPasswordConfirm.requestFocus()
                true
            } else false
        }

        etPasswordConfirm.addTextChangedListener(clearErrorOnInput)
        etPasswordConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister()
                true
            } else false
        }

        btnRegister.setOnClickListener { attemptRegister() }

        tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.registerScroll)) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                ime.bottom.coerceAtLeast(systemBars.bottom)
            )
            insets
        }
    }

    private fun attemptRegister() {
        if (isLoading) return

        val name = tilName.editText?.text?.toString().orEmpty()
        val email = tilEmail.editText?.text?.toString().orEmpty()
        val password = tilPassword.editText?.text?.toString().orEmpty()
        val confirmPassword = tilPasswordConfirm.editText?.text?.toString().orEmpty()

        tilName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilPasswordConfirm.error = null
        hideError()

        var hasError = false

        if (name.isBlank()) {
            tilName.error = getString(R.string.register_error_empty_name)
            hasError = true
        }

        if (email.isBlank()) {
            tilEmail.error = getString(R.string.register_error_empty_email)
            hasError = true
        }

        if (password.isBlank()) {
            tilPassword.error = getString(R.string.register_error_empty_password)
            hasError = true
        }

        if (confirmPassword.isBlank()) {
            tilPasswordConfirm.error = getString(R.string.register_error_empty_confirm_password)
            hasError = true
        }

        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            tilPasswordConfirm.error = getString(R.string.register_error_password_mismatch)
            hasError = true
        }

        if (hasError) return

        setLoading(true)

        val success = DataStore.registerUser(email, name, password)
        if (success) {
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.register_success),
                android.widget.Toast.LENGTH_LONG
            ).show()
            parentFragmentManager.popBackStack()
        } else {
            showError(getString(R.string.register_error_duplicate_email))
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        btnRegister.isEnabled = !loading
        if (loading) {
            btnRegister.text = ""
            btnRegister.setIconResource(android.R.drawable.ic_popup_sync)
            btnRegister.iconGravity = android.view.Gravity.CENTER
            btnRegister.iconSize = 28
        } else {
            btnRegister.text = getString(R.string.register_button)
            btnRegister.icon = null
        }
    }

    private fun showError(message: String) {
        tvRegisterError.text = message
        tvRegisterError.visibility = View.VISIBLE
        tvRegisterError.announceForAccessibility(message)
    }

    private fun hideError() {
        tvRegisterError.visibility = View.GONE
    }
}