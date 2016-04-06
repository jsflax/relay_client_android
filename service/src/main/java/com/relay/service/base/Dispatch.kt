package com.relay.service.base

import com.relay.service.RelayService

import kotlin.printStackTrace

/**
 * Created by jasonflax on 3/8/16.
 */
internal object Dispatch {
    fun <A> sendRequest(url: String,
                        request: Request,
                        host: String? = null,
                        handler: (Payload<A>) -> A?): Payload<A> {
        try {
            val result = Http("http://${host?:RelayService.config?.host}$url")
                .call(request)
                .getResponse()

            val payload = Payload<A>(result?.body)

            payload.parcel = handler(Payload(result?.body))

            return payload
        } catch (e: Exception) {
            e.printStackTrace()
            return Payload()
        }
    }
}
