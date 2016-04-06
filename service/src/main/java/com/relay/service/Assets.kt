package com.relay.service

import com.relay.data.Asset
import com.relay.service.base.Dispatch
import com.relay.service.base.GET
import com.relay.service.base.Payload

/**
 * @author jasonflax on 4/3/16.
 */
object Assets {
    fun avatars(): Payload<List<Asset>> {
        return Dispatch.sendRequest("/assets/avatars", GET()) {
            it.data?.asJsonArray?.map {
                Asset(toMap(it.asJsonObject),
                    "http://" + RelayService.config?.host)
            }
        }
    }
}
