package com.relay.data

/**
 * Datum for users that must create accounts
 * to access the API
 *
 * @param id        unique ID of user
 * @param name      username
 * @param email     email user signs up with
 * @param avatarUrl url of avatar
 * @param token     password hash
 *
 * @author jsflax on 3/30/16.
 */
data class User(val map: Map<String, Any?>) {
    constructor(id: Long,
                name: String,
                avatarUrl: String,
                token: String) :
    this(mapOf(
        "id" to id,
        "name" to name,
        "avatarUrl" to avatarUrl,
        "token" to token
    ))

    val id: Long by map
    val name: String by map
    val avatarUrl: String by map
    val token: String by map
}

data class UserCreateRequest(val map: Map<String, Any?>) {
    constructor(password: String,
                name: String,
                avatarUrl: String? = null) :
    this(mapOf(
        "password" to password,
        "name" to name,
        "avatarUrl" to avatarUrl
    ))

    val password: String by map
    val name: String by map
    val avatarUrl: String? by map
}

data class SimpleUuid(val map: Map<String, Any?>) {
    constructor(uuid: String):
        this(mapOf("uuid" to uuid))

    val uuid: String by map
}

data class UserLoginRequest(val map: Map<String, Any?>) {
    constructor(name: String,
                password: String) :
    this(mapOf("name" to name, "password" to password))

    val name: String by map
    val password: String by map
}
