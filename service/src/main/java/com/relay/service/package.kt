package com.relay.service

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

/**
 * @author jasonflax on 4/2/16.
 */
fun toMap(jsonObject: JsonObject): Map<String, Any?> =
    GsonBuilder().create().fromJson(
        jsonObject,
        mutableMapOf<String, Any?>().javaClass
    )
