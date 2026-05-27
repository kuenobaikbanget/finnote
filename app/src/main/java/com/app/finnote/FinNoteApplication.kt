package com.app.finnote

import android.app.Application
import com.app.finnote.data.AppContainer

class FinNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}
