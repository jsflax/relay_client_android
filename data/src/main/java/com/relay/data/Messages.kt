package com.relay.data

/**
 * @author jasonflax on 4/4/16.
 */
data class MessageRequest(val channelId: Int,
                          val rawContent: String,
                          val time: Long,
                          val userId: Long,
                          val username: String,
                          val avatarUrl: String)

data class Link(val link: String,
                val title: String)

data class MessageResponse(val map: Map<String, Any?>, val host: String) {
    constructor(channelId: Int,
                userId: Int,
                rawContent: String,
                mentions: Array<String>,
                emoticons: Array<String>,
                links: Array<Link>,
                strippedContent: String,
                username: String,
                avatarUrl: String,
                time: Long,
                host: String):
        this(mapOf(
            "channelId" to channelId,
            "userId" to userId,
            "rawContent" to rawContent,
            "mentions" to mentions,
            "emoticons" to emoticons,
            "links" to links,
            "strippedContent" to strippedContent,
            "username" to username,
            "avatarUrl" to avatarUrl,
            "time" to time
        ), host)

    val channelId: Int by map
    val userId: Int by map
    val rawContent: String by map
    val mentions: Array<String> by map
    val emoticons: Array<String> by map
    val links: Array<Link> by map
    val strippedContent: String by map
    val username: String by map
    private val _avatarUrl: String = map["avatarUrl"] as String
    val avatarUrl = "http://" + host + _avatarUrl
    val time: Long by map
}
