package com.jsflax.relay

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.jsflax.relay.core.ReduxStore
import com.relay.service.RelayService
import java.util.*

/**
 * @author jasonflax on 4/2/16.
 */
class RelayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        RelayService.init("192.168.1.145:9000")

        ReduxStore.init(this)

        Fresco.initialize(this)
    }
}
