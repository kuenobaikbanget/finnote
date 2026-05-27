package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.finnote.R

class ProfileFragment : Fragment() {
    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this, ProfileViewModel.Factory)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            val user = profile.user

            view.findViewById<TextView>(R.id.tvProfileName).text = user.name
            view.findViewById<TextView>(R.id.tvProfileEmail).text = user.email
            view.findViewById<TextView>(R.id.tvProfileJoined).text = user.joinedDate

            // Stats row data
            view.findViewById<TextView>(R.id.tvTransactionCount).text = profile.transactionCount.toString()
        }
        profileViewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.refresh()
    }
}
