package com.relay.service.base

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.collections.forEach
import kotlin.printStackTrace

/**
 * Created by jasonflax on 3/8/16.
 */
data class HttpResponse(val code: Int, val body: String?)

class Http(val url: String) {
    private var headers: Map<String, Any?>? = null
    private var httpUrlConnection: HttpURLConnection? = null
    private var request: Request? = null

    private fun getNewHttpUrlConnection(request: Request): HttpURLConnection {
        return when (request) {
            is GET -> URL("$url${request.render()}?")
            else -> URL(url)
        }.openConnection() as HttpURLConnection
    }

    fun call(request: Request): Http {
        this.request = request

        if (httpUrlConnection == null) {
            httpUrlConnection = getNewHttpUrlConnection(request)
        }

        httpUrlConnection?.requestMethod = request.methodName

        headers?.forEach {
            httpUrlConnection?.setRequestProperty(it.key, it.value.toString())
        }

        if (request is Entity) {
            try {
                val params = request.render()

                // add header for Content-Type
                httpUrlConnection?.setRequestProperty(
                    "Content-Type",
                    request.contentType.value
                )
                httpUrlConnection?.setRequestProperty(
                    "charset",
                    "utf-8"
                )
                httpUrlConnection?.setRequestProperty(
                    "Content-Length",
                    params.length.toString()
                )

                httpUrlConnection?.doOutput = true
                httpUrlConnection?.useCaches = false

                val os = httpUrlConnection?.outputStream

                os?.write(params.toByteArray(StandardCharsets.UTF_8))
                os?.flush()
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return this
    }

    fun getResponse(): HttpResponse? {
        if (request != null) {
            try {
                if (httpUrlConnection == null) {
                    httpUrlConnection = getNewHttpUrlConnection(request!!)
                }

                val stringBuilder = StringBuilder()

                return HttpResponse(
                    httpUrlConnection?.responseCode ?: 500,
                    if (httpUrlConnection?.responseCode ?: 500 == 200) {
                        val inputStream = httpUrlConnection?.inputStream
                        val rd = BufferedReader(
                            InputStreamReader(inputStream))
                        var line: String? = rd.readLine()
                        do {
                            stringBuilder.append(line)
                            line = rd.readLine()
                        } while (line != null)
                        stringBuilder.toString()
                    } else {
                        null
                    }
                )
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        } else {
            return null
        }
    }
}
