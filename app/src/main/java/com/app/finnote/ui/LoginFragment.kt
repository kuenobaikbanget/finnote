package com.app.finnote.ui

import android.content.Intent
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
import com.app.finnote.MainActivity
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvLoginError: TextView
    private lateinit var tvRegister: MaterialButton
    private lateinit var tvForgotPassword: MaterialButton

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvLoginError = view.findViewById(R.id.tvLoginError)
        tvRegister = view.findViewById(R.id.tvRegister)
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword)

        val etEmail = tilEmail.editText!!
        val etPassword = tilPassword.editText!!

        val clearErrorOnInput = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tvLoginError.visibility == View.VISIBLE) {
                    hideError()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
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
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }

        btnLogin.setOnClickListener { attemptLogin() }

        tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.authFragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        tvForgotPassword.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), getString(R.string.login_todo_message), android.widget.Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.loginScroll)) { v, insets ->
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

    private fun attemptLogin() {
        if (isLoading) return

        val email = tilEmail.editText?.text?.toString().orEmpty()
        val password = tilPassword.editText?.text?.toString().orEmpty()

        tilEmail.error = null
        tilPassword.error = null
        hideError()

        var hasError = false

        if (email.isBlank()) {
            tilEmail.error = getString(R.string.login_error_empty_email)
            hasError = true
        }

        if (password.isBlank()) {
            tilPassword.error = getString(R.string.login_error_empty_password)
            hasError = true
        }

        if (hasError) return

        setLoading(true)

        if (DataStore.loginUser(email, password)) {
            DataStore.setLoggedIn(email)
            navigateToMain()
        } else {
            showError(getString(R.string.login_error_invalid))
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        btnLogin.isEnabled = !loading
        if (loading) {
            btnLogin.text = ""
            btnLogin.setIconResource(android.R.drawable.ic_popup_sync)
            btnLogin.iconGravity = android.view.Gravity.CENTER
            btnLogin.iconSize = 28
        } else {
            btnLogin.text = getString(R.string.login_button)
            btnLogin.icon = null
        }
    }

    private fun showError(message: String) {
        tvLoginError.text = message
        tvLoginError.visibility = View.VISIBLE
        tvLoginError.announceForAccessibility(message)
    }

    private fun hideError() {
        tvLoginError.visibility = View.GONE
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}