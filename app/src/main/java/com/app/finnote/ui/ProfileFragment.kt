package com.app.finnote.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.app.finnote.LoginActivity
import com.app.finnote.MainActivity
import com.app.finnote.R
import com.app.finnote.data.DataStore

class ProfileFragment : Fragment() {

    private var logoutDialog: BottomSheetDialog? = null
    private var isLoggingOut = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyProfileInsets(view)

        val loggedIn = DataStore.isLoggedIn()
        val user = if (loggedIn) DataStore.getCurrentUser() else null

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvJoined = view.findViewById<TextView>(R.id.tvProfileJoined)
        val btnAction = view.findViewById<TextView>(R.id.btnLogout)
        val ivProfile = view.findViewById<android.widget.ImageView>(R.id.ivProfile)

        if (loggedIn && user != null) {
            tvName.text = user.name.ifBlank { getString(R.string.default_user_name) }
            tvEmail.visibility = View.VISIBLE
            tvEmail.text = user.email
            tvJoined.visibility = View.VISIBLE
            tvJoined.text = getString(R.string.profile_joined_format, user.joinedDate)
            btnAction.text = getString(R.string.profile_logout_button)
            btnAction.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.pale_red))
            btnAction.setBackgroundResource(R.drawable.bg_logout_ripple)
            bindLogout(view)
        } else {
            tvName.text = getString(R.string.guest_user_name)
            tvEmail.visibility = View.GONE
            tvJoined.visibility = View.GONE
            btnAction.text = getString(R.string.profile_login_button)
            btnAction.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.action_green_accessible))
            btnAction.setBackgroundResource(R.drawable.bg_login_ripple)
            ivProfile.setImageResource(R.drawable.ic_photo_profile_round)
            bindLogin(view)
        }

        bindStats(view)
    }

    private fun applyProfileInsets(view: View) {
        val titlePage = view.findViewById<View>(R.id.tvTitlePage)
        val initialTopMargin = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            titlePage.layoutParams = (titlePage.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = initialTopMargin + (statusBars.top / 2)
            }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun bindStats(view: View) {
        val loggedIn = DataStore.isLoggedIn()
        val transactions = if (loggedIn) DataStore.getAll() else emptyList()
        val monthKey = DataStore.getCurrentMonthKey()

        val tvTransactionCount = view.findViewById<TextView>(R.id.tvTransactionCount)
        val tvProfileTopCategory = view.findViewById<TextView>(R.id.tvProfileTopCategory)

        if (loggedIn) {
            tvTransactionCount.text = getString(R.string.profile_transaction_count_format, transactions.size)
            tvProfileTopCategory.text = getTopExpenseCategory(monthKey) ?: getString(R.string.profile_top_category_empty)
        } else {
            tvTransactionCount.text = "-"
            tvProfileTopCategory.text = "-"
        }
    }

    private fun bindLogin(view: View) {
        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            startActivity(Intent(ctx, LoginActivity::class.java))
        }
    }

    private fun bindLogout(view: View) {
        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            if (!isLoggingOut) showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val ctx = context ?: return
        if (!isAdded || isDetached) return

        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_logout, null)
        val sheetContent = sheetView.findViewById<View>(R.id.logoutSheetContent)
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

        val dialog = BottomSheetDialog(ctx).apply {
            setContentView(sheetView)
            setCanceledOnTouchOutside(true)
            setOnDismissListener { logoutDialog = null }
        }
        logoutDialog = dialog

        sheetView.findViewById<MaterialButton>(R.id.btnLogoutConfirm).setOnClickListener {
            if (isAdded && !isDetached && !isLoggingOut) {
                isLoggingOut = true
                dialog.dismiss()
                performLogout()
            }
        }
        sheetView.findViewById<MaterialButton>(R.id.btnLogoutCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.apply {
                setBackgroundColor(Color.TRANSPARENT)
                backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                (parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
            }
        ViewCompat.requestApplyInsets(sheetContent)
    }

    private fun performLogout() {
        DataStore.logout()
        (requireActivity() as? MainActivity)?.openHome()
    }

    private fun getTopExpenseCategory(monthKey: String): String? {
        return DataStore.getAll()
            .asSequence()
            .filter { transaction ->
                transaction.type == "expense" && transaction.date.startsWith(monthKey)
            }
            .groupBy { it.category }
            .maxByOrNull { groupedCategory ->
                groupedCategory.value.sumOf { it.amount }
            }
            ?.key
    }

    override fun onDestroyView() {
        logoutDialog?.dismiss()
        logoutDialog = null
        super.onDestroyView()
    }
}
