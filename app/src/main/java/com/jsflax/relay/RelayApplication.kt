package com.jsflax.relay

import android.app.Application
import com.relay.service.RelayService

/**
 * @author jasonflax on 4/2/16.
 */
class RelayApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        RelayService.init("http://localhost:9000")
    }
}
