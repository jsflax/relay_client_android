package com.relay.data

import java.util.*

/**
 * Shim to request a message be sent to a channel.
 *
 * @param channelId id of channel to be sent to
 * @param rawContent raw text data of message
 * @param time millis since epoch this message was sent
 * @param userId id of user this was sent by
 * @param username name of user this was sent by
 * @param avatarUrl avatar url of user sent for convenience
 *
 * @author jasonflax on 4/4/16.
 */
data class MessageRequest(val channelId: Int,
                          val rawContent: String,
                          val time: Long,
                          val userId: Int,
                          val username: String,
                          val avatarUrl: String)

/**
 * Datum for a link that can be receive from a message from the server.
 *
 * @param link actual url
 * @param title title of page
 */
data class Link(val link: String,
                val title: String)

/**
 * Datum for messges received from the server.
 */
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

    /**
     * Id of channel this was posted to
     */
    val channelId: Int by map
    /**
     * Id of user this was sent by
     */
    val userId: Int by map
    /**
     * Raw text content of message
     */
    val rawContent: String by map
    /**
     * Mentions of other users in the content
     */
    val mentions: List<String> by map
    /**
     * Emoticons referenced in the content
     */
    val emoticons: List<String> by map
    /**
     * Links referenced in the content
     */
    val links: List<Link> by map
    /**
     * Templated content based on the special
     * attributes (links, emoticons, mentions) associated with the content
     */
    val strippedContent: String by map
    /**
     * Username of the sender
     */
    val username: String by map
    private val _avatarUrl: String = map["avatarUrl"] as String
    val avatarUrl = "http://" + host + _avatarUrl
    val time: Long by map
}
