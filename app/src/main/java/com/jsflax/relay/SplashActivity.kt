package com.jsflax.relay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // display splash briefly, then move on
        // to main activity
        Handler(mainLooper).postDelayed({
            startActivity(
                Intent(this, MasterActivity::class.java)
            )
            finish()
        }, 500)
    }
}
