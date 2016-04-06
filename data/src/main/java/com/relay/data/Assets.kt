package com.relay.data

/**
 * @author jasonflax on 4/3/16.
 */
data class Asset(val map: Map<String, Any?>, val host: String) {
    val path: String by map
    val name: String by map
}
