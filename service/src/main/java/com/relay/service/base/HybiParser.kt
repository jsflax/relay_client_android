//
// HybiParser.java: draft-ietf-hybi-thewebsocketprotocol-13 parser
//
// Based on code from the faye project.
// https://github.com/faye/faye-websocket-node
// Copyright (c) 2009-2012 James Coglan
//
// Ported from Javascript to Java by Eric Butler <eric@codebutler.com>
//
// (The MIT License)
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.relay.service.base

import java.io.*
import java.util.Arrays
import java.util.logging.Level
import java.util.logging.Logger

class HybiParser(private val mClient: WebSocketClient) {

    private val mMasking = true

    private var mStage: Int = 0

    private var mFinal: Boolean = false
    private var mMasked: Boolean = false
    private var mOpcode: Int = 0
    private var mLengthSize: Int = 0
    private var mLength: Int = 0
    private var mMode: Int = 0

    private var mMask = ByteArray(0)
    private var mPayload = ByteArray(0)

    private var mClosed = false

    private val mBuffer = ByteArrayOutputStream()

    @Throws(IOException::class)
    fun start(stream: HappyDataInputStream) {
        while (true) {
            if (stream.available() == -1) break
            when (mStage) {
                0 -> parseOpcode(stream.readByte())
                1 -> parseLength(stream.readByte())
                2 -> parseExtendedLength(stream.readBytes(mLengthSize))
                3 -> {
                    mMask = stream.readBytes(4)
                    mStage = 4
                }
                4 -> {
                    mPayload = stream.readBytes(mLength)
                    emitFrame()
                    mStage = 0
                }
            }
        }
        mClient.listener.onDisconnect(0, "EOF")
    }

    @Throws(ProtocolError::class)
    private fun parseOpcode(data: Byte) {
        val rsv1 = data.toInt() and RSV1 == RSV1
        val rsv2 = data.toInt() and RSV2 == RSV2
        val rsv3 = data.toInt() and RSV3 == RSV3

        if (rsv1 || rsv2 || rsv3) {
            throw ProtocolError("RSV not zero")
        }

        mFinal = data.toInt() and FIN == FIN
        mOpcode = data.toInt() and OPCODE
        mMask = ByteArray(0)
        mPayload = ByteArray(0)

        if (!OPCODES.contains(mOpcode)) {
            throw ProtocolError("Bad opcode")
        }

        if (!FRAGMENTED_OPCODES.contains(mOpcode) && !mFinal) {
            throw ProtocolError("Expected non-final packet")
        }

        mStage = 1
    }

    private fun parseLength(data: Byte) {
        mMasked = data.toInt() and MASK == MASK
        mLength = data.toInt() and LENGTH

        if (mLength >= 0 && mLength <= 125) {
            mStage = if (mMasked) 3 else 4
        } else {
            mLengthSize = if (mLength == 126) 2 else 8
            mStage = 2
        }
    }

    @Throws(ProtocolError::class)
    private fun parseExtendedLength(buffer: ByteArray) {
        mLength = getInteger(buffer)
        mStage = if (mMasked) 3 else 4
    }

    fun frame(data: String): ByteArray? {
        return frame(data, OP_TEXT, -1)
    }

    fun frame(data: ByteArray): ByteArray? {
        return frame(data, OP_BINARY, -1)
    }

    private fun frame(data: ByteArray, opcode: Int, errorCode: Int): ByteArray? {
        return frame(data as Any, opcode, errorCode)
    }

    private fun frame(data: String, opcode: Int, errorCode: Int): ByteArray? {
        return frame(data as Any, opcode, errorCode)
    }

    private fun frame(data: Any, opcode: Int, errorCode: Int): ByteArray? {
        if (mClosed) return null

        Logger.getGlobal().log(Level.INFO, TAG, "Creating frame for: $data op: $opcode err: $errorCode")

        val buffer = if (data is String) decode(data) else data as ByteArray
        val insert = if (errorCode > 0) 2 else 0
        val length = buffer.size + insert
        val header = if (length <= 125) 2 else if (length <= 65535) 4 else 10
        val offset = header + if (mMasking) 4 else 0
        val masked = if (mMasking) MASK else 0
        val frame = ByteArray(length + offset)

        frame[0] = (FIN or opcode).toByte()

        if (length <= 125) {
            frame[1] = (masked or length).toByte()
        } else if (length <= 65535) {
            frame[1] = (masked or 126).toByte()
            frame[2] = Math.floor((length / 256).toDouble()).toByte()
            frame[3] = (length and BYTE).toByte()
        } else {
            frame[1] = (masked or 127).toByte()
            frame[2] = (Math.floor(length / Math.pow(2.0, 56.0)).toInt() and BYTE).toByte()
            frame[3] = (Math.floor(length / Math.pow(2.0, 48.0)).toInt() and BYTE).toByte()
            frame[4] = (Math.floor(length / Math.pow(2.0, 40.0)).toInt() and BYTE).toByte()
            frame[5] = (Math.floor(length / Math.pow(2.0, 32.0)).toInt() and BYTE).toByte()
            frame[6] = (Math.floor(length / Math.pow(2.0, 24.0)).toInt() and BYTE).toByte()
            frame[7] = (Math.floor(length / Math.pow(2.0, 16.0)).toInt() and BYTE).toByte()
            frame[8] = (Math.floor(length / Math.pow(2.0, 8.0)).toInt() and BYTE).toByte()
            frame[9] = (length and BYTE).toByte()
        }

        if (errorCode > 0) {
            frame[offset] = (Math.floor((errorCode / 256).toDouble()).toInt() and BYTE).toByte()
            frame[offset + 1] = (errorCode and BYTE).toByte()
        }
        System.arraycopy(buffer, 0, frame, offset + insert, buffer.size)

        if (mMasking) {
            val mask = byteArrayOf(Math.floor(Math.random() * 256).toByte(), Math.floor(Math.random() * 256).toByte(), Math.floor(Math.random() * 256).toByte(), Math.floor(Math.random() * 256).toByte())
            System.arraycopy(mask, 0, frame, header, mask.size)
            mask(frame, mask, offset)
        }

        return frame
    }

    fun ping(message: String) {
        mClient.send(frame(message, OP_PING, -1) ?: ByteArray(0))
    }

    fun close(code: Int, reason: String) {
        if (mClosed) return
        mClient.send(frame(reason, OP_CLOSE, code) ?: ByteArray(0))
        mClosed = true
    }

    @Throws(IOException::class)
    private fun emitFrame() {
        val payload = mask(mPayload, mMask, 0)
        val opcode = mOpcode

        if (opcode == OP_CONTINUATION) {
            if (mMode == 0) {
                throw ProtocolError("Mode was not set.")
            }
            mBuffer.write(payload)
            if (mFinal) {
                val message = mBuffer.toByteArray()
                if (mMode == MODE_TEXT) {
                    mClient.listener.onMessage(encode(message))
                } else {
                    mClient.listener.onMessage(message)
                }
                reset()
            }

        } else if (opcode == OP_TEXT) {
            if (mFinal) {
                val messageText = encode(payload)
                mClient.listener.onMessage(messageText)
            } else {
                mMode = MODE_TEXT
                mBuffer.write(payload)
            }

        } else if (opcode == OP_BINARY) {
            if (mFinal) {
                mClient.listener.onMessage(payload)
            } else {
                mMode = MODE_BINARY
                mBuffer.write(payload)
            }

        } else if (opcode == OP_CLOSE) {
            val code = if (payload.size >= 2) 256 * payload[0] + payload[1] else 0
            val reason = if (payload.size > 2) encode(slice(payload, 2)) else null
            Logger.getGlobal().log(Level.INFO, TAG, "Got close op! $code $reason")
            mClient.listener.onDisconnect(code, reason)

        } else if (opcode == OP_PING) {
            if (payload.size > 125) {
                throw ProtocolError("Ping payload too large")
            }
            Logger.getGlobal().log(Level.INFO, TAG, "Sending pong!!")
            mClient.sendFrame(frame(payload, OP_PONG, -1) ?: ByteArray(0))

        } else if (opcode == OP_PONG) {
            val message = encode(payload)
            // FIXME: Fire callback...
            Logger.getGlobal().log(Level.INFO, TAG, "Got pong! " + message)
        }
    }

    private fun reset() {
        mMode = 0
        mBuffer.reset()
    }

    private fun encode(buffer: ByteArray): String {
        try {
            return String(buffer)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }

    }

    private fun decode(string: String): ByteArray {
        try {
            return string.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }

    }

    @Throws(ProtocolError::class)
    private fun getInteger(bytes: ByteArray): Int {
        val i = byteArrayToLong(bytes, 0, bytes.size)
        if (i < 0 || i > Integer.MAX_VALUE) {
            throw ProtocolError("Bad integer: " + i)
        }
        return i.toInt()
    }

    private fun slice(array: ByteArray, start: Int): ByteArray {
        return Arrays.copyOfRange(array, start, array.size)
    }

    class ProtocolError(detailMessage: String) : IOException(detailMessage)

    class HappyDataInputStream(`in`: InputStream) : DataInputStream(`in`) {

        @Throws(IOException::class)
        fun readBytes(length: Int): ByteArray {
            val buffer = ByteArray(length)
            readFully(buffer)
            return buffer
        }
    }

    companion object {
        private val TAG = "HybiParser"

        private val BYTE = 255
        private val FIN = 128
        private val MASK = 128
        private val RSV1 = 64
        private val RSV2 = 32
        private val RSV3 = 16
        private val OPCODE = 15
        private val LENGTH = 127

        private val MODE_TEXT = 1
        private val MODE_BINARY = 2

        private val OP_CONTINUATION = 0
        private val OP_TEXT = 1
        private val OP_BINARY = 2
        private val OP_CLOSE = 8
        private val OP_PING = 9
        private val OP_PONG = 10

        private val OPCODES = Arrays.asList(
            OP_CONTINUATION,
            OP_TEXT,
            OP_BINARY,
            OP_CLOSE,
            OP_PING,
            OP_PONG)

        private val FRAGMENTED_OPCODES = Arrays.asList(
            OP_CONTINUATION, OP_TEXT, OP_BINARY)

        private fun mask(payload: ByteArray, mask: ByteArray, offset: Int): ByteArray {
            if (mask.size == 0) return payload

            for (i in 0..payload.size - offset - 1) {
                payload[offset + i] = (payload[offset + i].toInt() xor mask[i % 4].toInt()).toByte()
            }
            return payload
        }

        private fun byteArrayToLong(b: ByteArray, offset: Int, length: Int): Long {
            if (b.size < length)
                throw IllegalArgumentException("length must be less than or equal to b.length")

            var value: Long = 0
            for (i in 0..length - 1) {
                val shift = (length - 1 - i) * 8
                value += (b[i + offset].toInt() and 0x000000FF shl shift).toLong()
            }
            return value
        }
    }
}