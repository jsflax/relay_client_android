package com.relay.service

import com.relay.data.Emoticon
import com.relay.service.base.*

/**
 * @author jasonflax on 4/2/16.
 */
object Emoticons {
    fun get(): Payload<List<Emoticon>> {
        return Dispatch.sendRequest("/emoticons", GET()) {
            it.data?.asJsonArray?.map { Emoticon(toMap(it.asJsonObject)) }
        }
    }
}
