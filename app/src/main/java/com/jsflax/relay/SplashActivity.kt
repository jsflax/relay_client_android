package com.jsflax.relay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.jsflax.relay.MasterActivity

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(mainLooper).postDelayed({
            startActivity(
                Intent(this, MasterActivity::class.java)
            )
            finish()
        }, 500)
    }
}
