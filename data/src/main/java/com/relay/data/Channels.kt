package com.relay.data

import java.io.Serializable

/**
 * Datum to be used as a shim with the server to
 * create a new channel
 *
 * @author jasonflax on 4/2/16.
 */
data class ChannelCreateRequest(val map: Map<String, Any?>) {
    constructor(token: String,
                name: String,
                description: String):
        this(mapOf(
            "token" to token,
            "name" to name,
            "description" to description
        ))

    /**
     * Token associated with creator
     */
    val token: String by map
    /**
     * Proposed name of channel
     */
    val name: String by map
    /**
     * Proposed description of channel
     */
    val description by map
}

/**
 * Datum used as a shim with the server to
 * subscribe to a channel
 */
data class ChannelSubscribeRequest(val map: Map<String, Any?>) {
    constructor(token : String):
        this(mapOf("token" to token))

    /**
     * Token of subscriber
     */
    val token: String by map
}

/**
 * Raw channel datum, with basic information about a channel.
 *
 * A channel references an open chat that users can live post a
 * [[MessageRequest]] to.
 */
data class Channel(val map: Map<String, Any?>): Serializable {
    constructor(id: Int,
                creatorId: Int,
                name: String,
                dateCreated: Long,
                description: String):
        this(mapOf(
            "id" to id,
            "creatorId" to creatorId,
            "name" to name,
            "dateCreated" to dateCreated,
            "description" to description
        ))

    /**
     * Id of this channel
     */
    val id: Int by map
    /**
     * Id of the creator of this channel
     */
    val creatorId: Int by map
    /**
     * Name of this channel
     */
    val name: String by map
    /**
     * Date this channel was created
     */
    val dateCreated: Long by map
    /**
     * Description of this channel
     */
    val description: String by map
}
