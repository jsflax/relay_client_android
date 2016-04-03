package com.relay.data

/**
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

    val token: String by map
    val name: String by map
    val description by map
}

data class ChannelSubscribeRequest(val map: Map<String, Any?>) {
    constructor(token : String):
        this(mapOf("token" to token))

    val token: String by map
}

data class Channel(val map: Map<String, Any?>) {
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

    val id: Int by map
    val creatorId: Int by map
    val name: String by map
    val dateCreated: Long by map
    val description: String by map
}
