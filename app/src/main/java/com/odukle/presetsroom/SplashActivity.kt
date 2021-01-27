package com.odukle.presetsroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedPref = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val showIns = sharedPref.getBoolean(SHOW_SPLASH_INS, true)

        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                if (showIns) {
                    startActivity(Intent(this, HowToImportSplash::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }, 2000)
        }
    }
}