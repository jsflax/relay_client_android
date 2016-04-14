package com.relay.data

/**
 * Datum for an individual asset from the server
 *
 * @author jasonflax on 4/3/16.
 */
data class Asset(val map: Map<String, Any?>, val host: String) {
    /**
     * relative path to resource
     */
    val path: String by map
    /**
     * alphanumeric name of resource
     */
    val name: String by map
}
