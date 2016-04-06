package com.relay.service

import com.relay.data.User
import com.relay.data.UserCreateRequest
import com.relay.data.UserLoginRequest
import com.relay.service.base.*

/**
 * @author jasonflax on 4/3/16.
 */
object Users {
    fun login(userLoginRequest: UserLoginRequest): Payload<User> {
        return Dispatch.sendRequest("/users",
            POST(userLoginRequest.map, ContentType.APPLICATION_JSON)
        ) {
            User(toMap(it.data?.asJsonObject!!))
        }
    }

    fun signUp(userCreateRequest: UserCreateRequest): Payload<User> {
        return Dispatch.sendRequest("/users",
            PUT(userCreateRequest.map)
        ) {
            User(toMap(it.data?.asJsonObject!!))
        }
    }
}
