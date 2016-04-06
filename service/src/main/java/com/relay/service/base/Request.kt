package com.relay.service.base

import com.google.gson.GsonBuilder
import java.net.URLEncoder
import kotlin.collections.forEach
import kotlin.text.dropLast

/**
 * Created by jasonflax on 3/8/16.
 */

fun <K, V> Map<K, V>.mkString(mk: (Map.Entry<K, V>) -> String):
    String {
    val stringBuilder = StringBuilder()
    this.forEach { stringBuilder.append(mk.invoke(it)) }
    return stringBuilder.toString()
}

/**
 * Content type to send as a header over HTTP.

 * Created by jasonflax on 2/22/16.
 */
enum class ContentType internal constructor(internal val value: String) {
    /**
     * Body should be encoded as JSON
     */
    APPLICATION_JSON("application/json"),
    /**
     * Body should be encoded as form
     */
    APPLICATION_X_FORM("application/x-www-form-urlencoded");

    private fun getParamString(params: Map<String, Any?>): String =
        params.mkString({
            "${it.key}=" +
                "${URLEncoder.encode(it.value.toString(), "UTF-8")}&"
        }).dropLast(1)

    fun render(params: Map<String, Any?>): String {
        when (this) {
            APPLICATION_JSON ->
                return GsonBuilder().create().toJson(params)
            APPLICATION_X_FORM ->
                return getParamString(params)
        }
    }
}

abstract class Request(val methodName: String) {
    abstract fun render(): String
}

final class GET(val params: Map<String, Any?> = mapOf()): Request("GET") {
    override fun render(): String {
        return ContentType.APPLICATION_X_FORM.render(params);
    }
}

interface Entity {
    val contentType: ContentType
}

final class POST(val params: Map<String, Any?>,
                 override val contentType: ContentType =
                 ContentType.APPLICATION_X_FORM): Request("POST"), Entity {

    override fun render(): String = contentType.render(params)
}

final class PUT(val params: Map<String, Any?>,
                override val contentType: ContentType =
                ContentType.APPLICATION_JSON): Request("PUT"), Entity {

    override fun render(): String = contentType.render(params)
}