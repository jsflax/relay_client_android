package com.relay.service

import com.relay.data.Channel
import com.relay.service.base.Dispatch
import com.relay.service.base.GET
import com.relay.service.base.Payload

/**
 * @author jasonflax on 4/2/16.
 */
object Channels {
    fun get(): Payload<List<Channel>> {
        return Dispatch.sendRequest("/channels", GET()) {
            it.data?.asJsonArray?.map { Channel(toMap(it.asJsonObject)) }
        }
    }
}
