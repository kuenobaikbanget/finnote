package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = DataStore.currentUser
        val transactions = DataStore.transactions

        view.findViewById<TextView>(R.id.tvProfileName).text = user.name
        view.findViewById<TextView>(R.id.tvProfileEmail).text = user.email
        view.findViewById<TextView>(R.id.tvProfileJoined).text = user.joinedDate

        // Stats row data
        view.findViewById<TextView>(R.id.tvTransactionCount).text = transactions.size.toString()
    }
}
