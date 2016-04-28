package com.relay.service.base

import com.relay.service.RelayService
import org.apache.commons.codec.binary.Base64
import org.apache.http.Header
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.apache.http.message.BasicLineParser
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import java.io.EOFException
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.URI
import java.security.KeyManagementException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.logging.Level
import java.util.logging.Logger

class WebSocketClient(private val path: String,
                      val listener: WebSocketClient.Listener,
                      private val mExtraHeaders: Map<String, String> = mapOf()){
    private var mSocket: Socket? = null
    private var mThread: Thread? = null
    private val mParser: HybiParser
    private val mURI: URI
    private val mSendLock = Object()

    init {
        mParser = HybiParser(this)
        mURI = URI.create("ws://${RelayService.config?.host}$path")
    }

    fun connect() {
        if (mThread != null && mThread!!.isAlive) {
            return
        }

        mThread = Thread(Runnable {
            try {
                val secret = createSecret()

                val port = if (mURI.port != -1) mURI.port else if (mURI.scheme == "wss") 443 else 80

                var path = if (mURI.path.isEmpty()) "/" else mURI.path
                if (mURI.query != null && !mURI.query.isEmpty()) {
                    path += "?" + mURI.query
                }

                val originScheme = if (mURI.scheme == "wss") "https" else "http"
                val origin = URI(originScheme, "//" + mURI.host, null)

                val factory = SocketFactory.getDefault()
                mSocket = factory.createSocket(mURI.host, port)

                val out = PrintWriter(mSocket!!.outputStream)
                out.print("GET $path HTTP/1.1\r\n")
                out.print("Upgrade: websocket\r\n")
                out.print("Connection: Upgrade\r\n")
                out.print("Host: " + mURI.host + "\r\n")
                out.print("Origin: " + origin.toString() + "\r\n")
                out.print("Sec-WebSocket-Key: " + secret + "\r\n")
                out.print("Sec-WebSocket-Version: 13\r\n")
                if (mExtraHeaders.isNotEmpty()) {
                    for (pair in mExtraHeaders) {
                        out.print(String.format("%s: %s\r\n", pair.key, pair.value))
                    }
                }
                out.print("\r\n")
                out.flush()

                val stream = HybiParser.HappyDataInputStream(mSocket!!.inputStream)

                // Read HTTP response status line.
                val statusLine = parseStatusLine(readLine(stream)?:"")
                if (statusLine == null) {
                    throw Exception("Received no reply from server.")
                } else if (statusLine.statusCode !== HttpStatus.SC_SWITCHING_PROTOCOLS) {
                    throw Exception(statusLine.statusCode.toString() + ": " + statusLine.reasonPhrase)
                }

                // Read HTTP response headers.
                var line: String? = readLine(stream)
                var validated = false

                while (line != null && !line.isEmpty()) {
                    val header = parseHeader(line)
                    if (header.name.toLowerCase().equals(
                        "sec-websocket-accept"
                    )) {
                        val expected = createSecretValidation(secret)
                        val actual = header.value.trim()

                        if (expected != actual) {
                            throw Exception("Bad Sec-WebSocket-Accept header value.")
                        }

                        validated = true
                    }
                    line = readLine(stream)
                }

                if (!validated) {
                    throw Exception("No Sec-WebSocket-Accept header.")
                }

                listener.onConnect()

                // Now decode websocket frames.
                mParser.start(stream)

            } catch (ex: EOFException) {
                Logger.getGlobal().log(Level.SEVERE, TAG, "WebSocket EOF!")
                listener.onDisconnect(0, "EOF")

            } catch (ex: SSLException) {
                // Connection reset by peer
                Logger.getGlobal().log(Level.SEVERE, TAG, "Websocket SSL error!")
                listener.onDisconnect(0, "SSL")

            } catch (ex: Exception) {
                listener.onError(ex)
            }
        })
        mThread!!.start()
    }

    fun disconnect() {
        if (mSocket != null) {
            Thread(Runnable {
                try {
                    mSocket!!.close()
                    mSocket = null
                } catch (ex: IOException) {
                    Logger.getGlobal().log(Level.SEVERE, TAG, "Error while disconnecting")
                    listener.onError(ex)
                }
            }).start()
        }
    }

    fun send(data: String) {
        sendFrame(mParser.frame(data)?: ByteArray(0))
    }

    fun send(data: ByteArray) {
        sendFrame(mParser.frame(data)?: ByteArray(0))
    }

    private fun parseStatusLine(line: String): StatusLine? {
        if (line.isEmpty()) {
            return null
        }
        return BasicLineParser.parseStatusLine(line, BasicLineParser())
    }

    private fun parseHeader(line: String): Header {
        return BasicLineParser.parseHeader(line, BasicLineParser())
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    @Throws(IOException::class)
    private fun readLine(reader: HybiParser.HappyDataInputStream): String? {
        var readChar = reader.read()
        if (readChar == -1) {
            return null
        }
        val string = StringBuilder("")
        while (readChar.toChar() != '\n') {
            if (readChar.toChar() != '\r') {
                string.append(readChar.toChar())
            }

            readChar = reader.read()
            if (readChar == -1) {
                return null
            }
        }
        return string.toString()
    }

    private fun createSecret(): String {
        val nonce = ByteArray(16)
        for (i in 0..15) {
            nonce[i] = (Math.random() * 256).toByte()
        }
        return String(Base64.encodeBase64(nonce)).trim()
    }

    private fun createSecretValidation(secret: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-1")
            md.update((secret + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray())
            return String(Base64.encodeBase64(md.digest())).trim()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    fun sendFrame(frame: ByteArray) {
        Thread(Runnable {
            try {
                synchronized (mSendLock) {
                    if (mSocket == null) {
                        throw IllegalStateException("Socket not connected")
                    }
                    val outputStream = mSocket!!.outputStream
                    outputStream.write(frame)
                    outputStream.flush()
                }
            } catch (e: IOException) {
                listener.onError(e)
            }
        }).start()
    }

    interface Listener {
        fun onConnect()
        fun onMessage(message: String)
        fun onMessage(data: ByteArray)
        fun onDisconnect(code: Int, reason: String?)
        fun onError(error: Exception)
    }

    private val sslSocketFactory: SSLSocketFactory
        @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
        get() {
            val context = SSLContext.getInstance("TLS")
            context.init(null, sTrustManagers, null)
            return context.socketFactory
        }

    companion object {
        private val TAG = "WebSocketClient"

        private var sTrustManagers: Array<TrustManager>? = null

        fun setTrustManagers(tm: Array<TrustManager>) {
            sTrustManagers = tm
        }
    }
}
