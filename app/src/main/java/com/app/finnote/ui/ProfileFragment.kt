package com.app.finnote.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.app.finnote.R
import com.app.finnote.data.DataStore

class ProfileFragment : Fragment() {

    private var editDialog: BottomSheetDialog? = null
    private var pendingAvatarUri: Uri? = null

    // Galeri picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        pendingAvatarUri = uri
        editDialog?.findViewById<ImageView>(R.id.ivEditAvatar)?.setImageURI(uri)
    }

    // Permission launcher (API 33+)
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) pickImage.launch("image/*") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyProfileInsets(view)
        bindProfile(view)
        bindStats(view)

        // Tap area nama → buka sheet edit profil
        view.findViewById<View>(R.id.profileCard).setOnClickListener { showEditDialog() }
        view.findViewById<View>(R.id.tvProfileName).setOnClickListener { showEditDialog() }
        // Tap avatar → langsung buka sheet edit profil (avatar di card adalah FrameLayout yang clickable)
        view.findViewById<View>(R.id.avatarContainer).setOnClickListener { showEditDialog() }
    }

    private fun bindProfile(view: View) {
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvJoined = view.findViewById<TextView>(R.id.tvProfileJoined)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)

        val name = DataStore.getUserName()
        if (name.isBlank()) {
            // State belum diisi: tampil sebagai placeholder muted, bukan bold hitam
            tvName.text = getString(R.string.profile_setup_name_hint)
            tvName.setTextColor(resources.getColor(R.color.text_muted_accessible, null))
            tvName.setTypeface(tvName.typeface, android.graphics.Typeface.NORMAL)
            tvName.textSize = 15f
        } else {
            tvName.text = name
            tvName.setTextColor(resources.getColor(R.color.black, null))
            tvName.setTypeface(null, android.graphics.Typeface.BOLD)
            tvName.textSize = 18f
        }

        val joined = DataStore.getJoinedDate()
        if (joined.isNotBlank()) {
            tvJoined.visibility = View.VISIBLE
            tvJoined.text = getString(R.string.profile_joined_format, joined)
        } else {
            tvJoined.visibility = View.GONE
        }

        loadAvatar(ivProfile)
    }

    private fun loadAvatar(iv: ImageView) {
        val uri = DataStore.getAvatarUri()
        if (uri != null) {
            try {
                iv.setImageURI(Uri.parse(uri))
            } catch (_: Exception) {
                iv.setImageResource(R.drawable.ic_photo_profile_round)
            }
        } else {
            iv.setImageResource(R.drawable.ic_photo_profile_round)
        }
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
        val transactions = DataStore.getAll()
        val monthKey = DataStore.getCurrentMonthKey()
        view.findViewById<TextView>(R.id.tvTransactionCount).text =
            getString(R.string.profile_transaction_count_format, transactions.size)
        view.findViewById<TextView>(R.id.tvProfileTopCategory).text =
            getTopExpenseCategory(monthKey) ?: getString(R.string.profile_top_category_empty)
    }

    private fun getTopExpenseCategory(monthKey: String): String? =
        DataStore.getAll()
            .filter { it.type == "expense" && it.date.startsWith(monthKey) }
            .groupBy { it.category }
            .maxByOrNull { it.value.sumOf { t -> t.amount } }
            ?.key

    // ── Edit profile bottom sheet ─────────────────────────

    private fun showEditDialog() {
        val ctx = context ?: return
        if (!isAdded || isDetached) return

        pendingAvatarUri = null

        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_edit_profile, null)
        val sheetContent = sheetView.findViewById<View>(R.id.editProfileSheetContent)
        val initialPaddingBottom = sheetContent.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(sheetContent) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft, v.paddingTop, v.paddingRight,
                initialPaddingBottom + maxOf(navBars.bottom, ime.bottom)
            )
            insets
        }

        val tilName = sheetView.findViewById<TextInputLayout>(R.id.tilEditName)
        val etName = tilName.editText!!
        val ivEditAvatar = sheetView.findViewById<ImageView>(R.id.ivEditAvatar)
        val avatarPickerCard = sheetView.findViewById<View>(R.id.avatarPickerCard)

        // Pre-populate
        etName.setText(DataStore.getUserName())
        etName.setSelection(etName.text?.length ?: 0)
        loadAvatar(ivEditAvatar)

        avatarPickerCard.setOnClickListener { launchImagePicker() }

        val dialog = BottomSheetDialog(ctx).apply {
            setContentView(sheetView)
            setCanceledOnTouchOutside(true)
            setOnDismissListener { editDialog = null }
        }
        editDialog = dialog

        // Konfigurasi window (warna nav bar, dsb.) harus di-set SEBELUM show(),
        // supaya sistem tidak sempat menggambar scrim kontras transparan dulu
        // sebelum kita override jadi putih solid.
        dialog.window?.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Dialog window tidak otomatis punya flag ini seperti Activity, sehingga
            // navigationBarColor di bawah tidak akan berefek tanpa baris ini.
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            // Warna nav bar putih solid (bukan transparan) supaya home indicator
            // menyatu penuh dengan bottom sheet, tanpa efek dim/scrim di baliknya.
            navigationBarColor = ContextCompat.getColor(ctx, R.color.white)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }
            // Hilangkan hairline divider bawaan sistem antara konten & nav bar
            // (muncul di API 28+), yang membuat seam terlihat meski warnanya sama.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                navigationBarDividerColor = ContextCompat.getColor(ctx, R.color.white)
            }
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightNavigationBars = true
        }

        sheetView.findViewById<MaterialButton>(R.id.btnEditProfileSave).setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            DataStore.setUserName(name)
            pendingAvatarUri?.let { DataStore.setAvatarUri(it.toString()) }
            dialog.dismiss()
            view?.let { bindProfile(it); bindStats(it) }
        }
        sheetView.findViewById<MaterialButton>(R.id.btnEditProfileCancel).setOnClickListener {
            dialog.dismiss()
        }

        etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sheetView.findViewById<MaterialButton>(R.id.btnEditProfileSave).performClick()
                true
            } else false
        }

        dialog.show()
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.apply {
                setBackgroundColor(Color.TRANSPARENT)
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                (parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
            }
        ViewCompat.requestApplyInsets(sheetContent)
    }

    private fun launchImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch("image/*")
            } else {
                requestPermission.launch(perm)
            }
        } else {
            val perm = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch("image/*")
            } else {
                requestPermission.launch(perm)
            }
        }
    }

    override fun onDestroyView() {
        editDialog?.dismiss()
        editDialog = null
        super.onDestroyView()
    }
}
