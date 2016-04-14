package com.relay.data

/**
 * Shim to relay emoticon information between the client
 * and server.
 *
 * @author jsflax on 4/5/16.
 */
data class Emoticon(val map: Map<String, Any?>) {
    constructor(id: Long,
                shortcut: String,
                type: String,
                url: String): this(mapOf(
        "id" to id,
        "shortcut" to shortcut,
        "type" to type,
        "url" to url
    ))

    /**
     * Id of this emoticon
     */
    val id: Long by map
    /**
     * Name of this emoticon
     */
    val shortcut: String by map
    /**
     * Type of emoticon
     */
    val type: String by map
    /**
     * Link to the image of this emoticon
     */
    val url: String by map
}
