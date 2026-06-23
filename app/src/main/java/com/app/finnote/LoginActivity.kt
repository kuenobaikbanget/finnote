package com.app.finnote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.app.finnote.data.DataStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvLoginError: android.widget.TextView
    private lateinit var tvRegister: MaterialButton
    private lateinit var tvForgotPassword: MaterialButton

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataStore.init(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_login)

        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvLoginError = findViewById(R.id.tvLoginError)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

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
            Toast.makeText(this, getString(R.string.login_todo_message), Toast.LENGTH_SHORT).show()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, getString(R.string.login_todo_message), Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScroll)) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
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
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
