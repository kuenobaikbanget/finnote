package com.app.finnote.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordFragment : Fragment() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var btnSubmit: MaterialButton
    private lateinit var tvForgotError: TextView
    private lateinit var tvBackToLogin: MaterialButton

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilEmail = view.findViewById(R.id.tilEmail)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        tvForgotError = view.findViewById(R.id.tvForgotError)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        val etEmail = tilEmail.editText!!

        val clearErrorOnInput = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tvForgotError.visibility == View.VISIBLE) {
                    hideError()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etEmail.addTextChangedListener(clearErrorOnInput)
        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSubmit()
                true
            } else false
        }

        btnSubmit.setOnClickListener { attemptSubmit() }

        tvBackToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.forgotScroll)) { v, insets ->
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

    private fun attemptSubmit() {
        if (isLoading) return

        val email = tilEmail.editText?.text?.toString().orEmpty().trim()

        tilEmail.error = null
        hideError()

        if (email.isBlank()) {
            tilEmail.error = getString(R.string.forgot_password_error_empty)
            return
        }

        setLoading(true)

        // Verifikasi apakah email terdaftar di database kita
        val emailExists = DataStore.isEmailExists(email)

        if (emailExists) {
            Toast.makeText(
                requireContext(),
                getString(R.string.forgot_password_success),
                Toast.LENGTH_LONG
            ).show()
            parentFragmentManager.popBackStack()
        } else {
            showError(getString(R.string.forgot_password_error_not_found))
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        btnSubmit.isEnabled = !loading
        if (loading) {
            btnSubmit.text = ""
            btnSubmit.setIconResource(android.R.drawable.ic_popup_sync)
            btnSubmit.iconGravity = android.view.Gravity.CENTER
            btnSubmit.iconSize = 28
        } else {
            btnSubmit.text = getString(R.string.forgot_password_button)
            btnSubmit.icon = null
        }
    }

    private fun showError(message: String) {
        tvForgotError.text = message
        tvForgotError.visibility = View.VISIBLE
        tvForgotError.announceForAccessibility(message)
    }

    private fun hideError() {
        tvForgotError.visibility = View.GONE
    }
}