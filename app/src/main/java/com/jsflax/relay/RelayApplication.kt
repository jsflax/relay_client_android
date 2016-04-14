package com.jsflax.relay

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.jsflax.relay.core.ReduxStore
import com.relay.service.RelayService

/**
 * @author jasonflax on 4/2/16.
 */
class RelayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // initialize service layer
        RelayService.init("young-mountain-16786.herokuapp.com")

        // initialize core layer
        ReduxStore.init(this)

        // initialize fresco (image loading) library
        Fresco.initialize(this)
    }
}
