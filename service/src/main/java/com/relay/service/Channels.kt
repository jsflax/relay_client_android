package com.relay.service

import com.relay.data.Channel
import com.relay.data.ChannelCreateRequest
import com.relay.data.SimpleUuid
import com.relay.service.base.*

/**
 * @author jasonflax on 4/2/16.
 */
object Channels {
    fun get(): Payload<List<Channel>> {
        return Dispatch.sendRequest("/channels", GET()) {
            it.data?.asJsonArray?.map { Channel(toMap(it.asJsonObject)) }
        }
    }

    fun create(createRequest: ChannelCreateRequest): Payload<Channel> {
        return Dispatch.sendRequest("/channels", PUT(
            createRequest.map,
            ContentType.APPLICATION_JSON
        )) {
            Channel(toMap(it.data?.asJsonObject!!))
        }
    }

    fun subscribe(id: Int, simpleUuid: SimpleUuid): Payload<Boolean> {
        return Dispatch.sendRequest("/channels/$id", POST(
            simpleUuid.map,
            ContentType.APPLICATION_JSON
        )) {
            it.status == 200
        }
    }
}
