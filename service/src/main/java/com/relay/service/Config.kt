package com.relay.service

internal class Config(val host: String) {
}

/**
 * Single entry point for constructing a request on the service layer
 */
object RelayService {
    private var _config: Config? = null
    internal var config: Config?
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
    }

    val channels = Channels
}
