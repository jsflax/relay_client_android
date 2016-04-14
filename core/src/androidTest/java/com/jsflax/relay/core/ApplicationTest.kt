package com.jsflax.relay.core

import android.app.Application
import android.test.ApplicationTestCase
import android.test.mock.MockContext
import com.google.gson.JsonParser
import com.relay.data.Emoticon
import com.relay.data.MessageResponse
import com.relay.service.Emoticons
import com.relay.service.RelayService
import com.relay.service.toMap
import org.junit.Test

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
class ApplicationTest :
    ApplicationTestCase<Application>(Application::class.java) {

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
    fun sayHello() {
        ChatSomeFragment.generateSpannableStringFromMessageResponse(
            context,
            MessageResponse(toMap(
                JsonParser().parse(resp).asJsonObject
            ), "localhost:9000"),
            emoticons
        )
    }
}