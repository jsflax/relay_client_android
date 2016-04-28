package com.relay.service

import com.relay.service.base.Dispatch
import com.relay.service.base.GET

class Config(val host: String) {
}

/**
 * Single entry point for constructing a request on the service layer
 */
object RelayService {
    private var _config: Config? = null
    var config: Config?
        get() {
            if (_config == null) {
                throw Exception("RelayService not initialized!")
            }

            return _config
        }
        set(value) {
            _config = value
        }

    fun init(host: String) {
        config = Config(host)

        Thread {
            // ping host
            Dispatch.sendRequest<Any>("", GET()) {}
        }
    }

    val channels = Channels
}
