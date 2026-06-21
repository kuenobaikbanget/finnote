package com.app.finnote

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.app.finnote.data.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private companion object {
        const val ANIM_LOGO_DURATION = 500L
        const val ANIM_NAME_DELAY = 250L
        const val ANIM_NAME_DURATION = 400L
        const val ANIM_TAGLINE_DELAY = 700L
        const val ANIM_TAGLINE_DURATION = 400L
        const val MIN_DISPLAY_MS = 2200L
        const val LOADING_DOTS_DELAY = 2500L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        DataStore.init(this)

        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<View>(R.id.ivSplashLogo)
        val tvName = findViewById<View>(R.id.tvSplashName)
        val tvTagline = findViewById<View>(R.id.tvSplashTagline)
        val pbLoading = findViewById<View>(R.id.pbSplashLoading)

        animateLogo(ivLogo)
        animateName(tvName)
        animateTagline(tvTagline)
        scheduleLoadingDots(pbLoading)

        lifecycleScope.launch {
            val minDelay = async { delay(MIN_DISPLAY_MS) }
            val authResult = async(Dispatchers.IO) { DataStore.isLoggedIn() }

            val loggedIn = authResult.await()
            minDelay.await()

            navigate(loggedIn)
        }
    }

    private fun animateLogo(view: View) {
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ANIM_LOGO_DURATION)
            .setInterpolator(OvershootInterpolator(0.4f))
            .start()
    }

    private fun animateName(view: View) {
        view.animate()
            .alpha(1f)
            .setStartDelay(ANIM_NAME_DELAY)
            .setDuration(ANIM_NAME_DURATION)
            .start()
    }

    private fun animateTagline(view: View) {
        view.animate()
            .alpha(1f)
            .setStartDelay(ANIM_TAGLINE_DELAY)
            .setDuration(ANIM_TAGLINE_DURATION)
            .start()
    }

    private fun scheduleLoadingDots(view: View) {
        handler.postDelayed({
            if (!navigated) {
                view.visibility = View.VISIBLE
                view.animate().alpha(1f).setDuration(200L).start()
            }
        }, LOADING_DOTS_DELAY)
    }

    private fun navigate(loggedIn: Boolean) {
        if (navigated) return
        navigated = true

        handler.removeCallbacksAndMessages(null)

        val target = if (loggedIn) MainActivity::class.java else LoginActivity::class.java
        startActivity(
            Intent(this, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
