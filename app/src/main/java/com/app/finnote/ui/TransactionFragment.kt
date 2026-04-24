package com.app.finnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.finnote.R
import com.app.finnote.data.DataStore
import com.app.finnote.ui.component.RecentTransactionsView

class TransactionFragment : Fragment() {
    private var recentView: RecentTransactionsView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentView = view.findViewById(R.id.containerRecent)
        recentView?.bindData(DataStore.getAll())
    }

    override fun onResume() {
        super.onResume()
        recentView?.bindData(DataStore.getAll())
    }
}
