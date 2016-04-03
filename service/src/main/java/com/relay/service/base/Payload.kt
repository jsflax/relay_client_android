package com.relay.service.base

import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Created by jasonflax on 3/8/16.
 */
class Payload<A>() {
    var parcel: A? = null
    var response: String? = null

    internal val json: JsonElement? by lazy {
        JsonParser().parse(response?:"{}")
    }

    internal val data: JsonElement? by lazy {
        json?.asJsonObject?.get("data")?:json?.asJsonObject
    }

    val status: Int by lazy {
        json?.asJsonObject?.get("success")?.asInt?: 500
    }

    val message: String by lazy {
        json?.asJsonObject?.get("message")?.asString?:
            "Unfortunately, an error has occurred"
    }

    constructor(a: A?): this() {
        this.parcel = a
    }

    constructor(response: String?): this() {
        this.response = response
    }

    override fun toString(): String = response?:super.toString()
}
