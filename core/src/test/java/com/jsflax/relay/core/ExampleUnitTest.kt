package com.jsflax.relay.core

import android.test.mock.MockContext
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.relay.data.Emoticon
import com.relay.data.MessageResponse
import com.relay.service.Emoticons
import com.relay.service.RelayService
import com.relay.service.toMap
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ExampleUnitTest {
    val resp = """
    {
        "channelId":1,
        "userId":1,
        "rawContent":"hey (hodor)",
        "mentions":[],
        "emoticons":["(hodor)"],
        "links":[],
        "strippedContent":"hey ${"\$e0"} ",
        "username":"zod",
        "avatarUrl":"",
        "time":1460138097608
    }
    """

    val emoticons: Array<Emoticon>
    init {
        // initialize service layer
        RelayService.init("localhost:9000")

        emoticons = Emoticons.get().parcel?.toTypedArray()!!
    }

    @Test
    @Throws(Exception::class)
    fun spannableMessage_isGenerated() {
        ChatSomeFragment.generateSpannableStringFromMessageResponse(
            MockContext(),
            MessageResponse(toMap(
                JsonParser().parse(resp).asJsonObject
            ), "localhost:9000"),
            emoticons
        )
    }
}
