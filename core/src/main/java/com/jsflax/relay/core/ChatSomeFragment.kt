package com.jsflax.relay.core

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.*
import android.text.style.ImageSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.datasource.DataSubscriber
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.image.CloseableBitmap
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.jsflax.relay.ui.ChatAdapter
import com.relay.data.Emoticon
import com.relay.data.MessageRequest
import com.relay.data.MessageResponse

/**
 * @author jasonflax on 4/4/16.
 */
class ChatSomeFragment : Fragment() {
    companion object {
        fun generateSpannableStringFromMessageResponse(
            context: Context,
            messageResponse: MessageResponse,
            emoticons: Array<Emoticon>,
            callback: (SpannableStringBuilder) -> Unit
        ) {

            val parseEmoteRegex = "(?<![^\\W\\d_])\\\$e\\w+".toRegex()

            val spannableString = SpannableStringBuilder(
                messageResponse.strippedContent
            )

            println(spannableString)

            var i = 0
            var j = 0

            parseEmoteRegex.findAll(messageResponse.strippedContent).forEach {
                val emoticon = emoticons.find {
                    it.shortcut.equals(messageResponse.emoticons[i])
                }

                println(spannableString)

                println(it.range)
                i++

                val imagePipeline = Fresco.getImagePipeline()
                imagePipeline.fetchDecodedImage(
                    ImageRequestBuilder.newBuilderWithSource(
                        Uri.parse(emoticon?.url)).build(), context
                ).subscribe(object : DataSubscriber<CloseableReference<CloseableImage>> {
                    override fun onNewResult(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        val image = dataSource?.result?.get()

                        if (image is CloseableBitmap) {
                            // do something with the bitmap

                            val bitmap = Bitmap.createScaledBitmap(
                                image.underlyingBitmap,
                                image.underlyingBitmap.width * 2,
                                image.underlyingBitmap.height * 2,
                                true
                            )

                            val imageSpan = ImageSpan(
                                context,
                                bitmap
                            )

                            spannableString.setSpan(
                                imageSpan,
                                it.range.start,
                                it.range.last + 1,
                                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            j++

                            if (j == messageResponse.emoticons.size) {
                                callback.invoke(spannableString)
                            }
                        }

                    }

                    override fun onCancellation(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        j++
                    }

                    override fun onProgressUpdate(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                    }

                    override fun onFailure(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        j++
                    }
                }, AsyncTask.SERIAL_EXECUTOR);
            }


            println(spannableString)
        }
    }

    /**
     * Chat adapter that will house our messages
     */
    val adapter by lazy {
        ChatAdapter(
            LayoutInflater.from(context),
            ReduxStore.user?.id ?: -1
        )
    }

    /**
     * Convenience method to remove existing emoticon fragment
     */
    fun removeEmoticonFragment() {
        val emoticonFragment = getEmoticonFragment()
        if (emoticonFragment != null) {
            fragmentManager.beginTransaction()
                .remove(emoticonFragment)
                .commit()
        }
    }

    fun addEmoticonFragment() =
        fragmentManager.beginTransaction()
            .add(R.id.chat_content, EmoticonSelectionFragment(), "emoticon")
            .commit()

    fun getEmoticonFragment(): EmoticonSelectionFragment? =
        fragmentManager.findFragmentByTag("emoticon") as?
            EmoticonSelectionFragment

    fun applyEmoticons(charSequence: CharSequence) {
        val emotes = ReduxStore.emoticons.filter {
            it.shortcut.startsWith(charSequence)
        }.toTypedArray()
        getEmoticonFragment()?.applyEmoticons(emotes)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view == null) {
            view = inflater?.inflate(
                R.layout.fg_chat_some, container, false
            )

            val recyclerView =
                view?.findViewById(R.id.chat_recycler_view) as RecyclerView

            val layoutManager = LinearLayoutManager(context);
            layoutManager.stackFromEnd = true;
            recyclerView.layoutManager = layoutManager

            val messageEditText =
                view?.findViewById(R.id.message_edit_text) as EditText


            messageEditText.addTextChangedListener(object : TextWatcher {
                var inEmoticonState = false
                var oldStart = -1
                var hasBeenManuallyChanged = false;
                var emoticonInProgress: CharSequence = ""
                var emoticonStart = -1

                override fun afterTextChanged(s: Editable?) {
                }

                override fun onTextChanged(s: CharSequence?,
                                           start: Int,
                                           before: Int,
                                           count: Int) {
                    Log.d(
                        "TextWatcher",
                        "seq: $s start: $start before: $before count: $count"
                    )

                    ReduxStore.subscribe("emoticonWatcher", {
                        when (it) {
                            Action.SelectedEmoticon -> {
                                hasBeenManuallyChanged = true
                                val emoticon = it.associatedData as Emoticon
                                messageEditText.setText(
                                    messageEditText.text.toString() +
                                        emoticon.shortcut.removePrefix(
                                            emoticonInProgress
                                        ) + ") "
                                )

                                removeEmoticonFragment()
                            }
                            else -> {
                            }
                        }
                    })

                    if (!hasBeenManuallyChanged) {
                        // if user adds a character
                        if (count > before) {
                            // if started a new character sequence
                            // else continuing in the current one
                            if (s?.get(start) == '(') {
                                Log.d(
                                    "TextWatcher",
                                    "entering emoticon state"
                                )
                                addEmoticonFragment()
                                inEmoticonState = true
                                emoticonStart = start + 1
                            } else if (s?.get(start) == ')') {
                                Log.d(
                                    "TextWatcher",
                                    "exiting emoticon state"
                                )
                                removeEmoticonFragment()
                                inEmoticonState = false
                            } else if (inEmoticonState) {
                                emoticonInProgress =
                                    s?.slice(emoticonStart..start + count - 1) ?: ""
                                applyEmoticons(emoticonInProgress)
                            }
                        } else {
                            if (inEmoticonState) {
                                emoticonInProgress =
                                    s?.slice(emoticonStart..start + count - 1) ?: ""
                                applyEmoticons(emoticonInProgress)
                            }
                        }

                        oldStart = start
                        hasBeenManuallyChanged = false
                    }
                }

                override fun beforeTextChanged(s: CharSequence?,
                                               start: Int,
                                               count: Int,
                                               after: Int) {
                    Log.w("TextWatcher", "seq: $s start: $start after: $after count: $count")

                    // if we've deleted a character
                    if (after < count) {
                        if (inEmoticonState) {
                            if (s?.get(start) == '(') {
                                Log.d("TextWatcher", "exiting emoticon state")
                                removeEmoticonFragment()
                                inEmoticonState = false
                            }
                        }
                    }
                }
            })

            recyclerView.adapter = adapter

            view?.findViewById(R.id.send)?.setOnClickListener {
                ReduxStore.dispatch(
                    Action.SendMessage,
                    MessageRequest(
                        ReduxStore.subscribedChannel?.id ?: -1,
                        messageEditText.text.toString(),
                        System.currentTimeMillis(),
                        ReduxStore.user?.id ?: -1,
                        ReduxStore.user?.name ?: "",
                        ReduxStore.user?.avatarUrl ?: ""
                    )
                )

                messageEditText.text.clear()
            }

            ReduxStore.subscribe("ChatSomeFragment", { action ->
                when (action) {
                    Action.ReceiveMessage -> {
                        generateSpannableStringFromMessageResponse(
                            context,
                            action.associatedData as MessageResponse,
                            ReduxStore.emoticons
                        ) { spannable ->
                            activity?.runOnUiThread {
                                adapter.addMessage(
                                    action.associatedData as MessageResponse to
                                        spannable
                                )
                            }
                        }
                    }
                    else -> {
                    }
                }
            })
        }

        return view
    }
}
