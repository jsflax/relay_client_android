package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.jsflax.relay.ui.ChatAdapter
import com.relay.data.MessageRequest
import com.relay.data.MessageResponse

/**
 * @author jasonflax on 4/4/16.
 */
class ChatSomeFragment: Fragment() {
    /**
     * Chat adapter that will house our messages
     */
    val adapter by lazy {
        ChatAdapter(
            LayoutInflater.from(context),
            ReduxStore.user?.id?.toInt() ?: -1
        )
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

            val messageEditText = view?.findViewById(R.id.message_edit_text) as EditText

            recyclerView.adapter = adapter

            view?.findViewById(R.id.send)?.setOnClickListener {
                ReduxStore.dispatch(
                    Action.SendMessage,
                    MessageRequest(
                        ReduxStore.subscribedChannel?.id?:-1,
                        messageEditText.text.toString(),
                        System.currentTimeMillis(),
                        ReduxStore.user?.id?:-1,
                        ReduxStore.user?.name?:"",
                        ReduxStore.user?.avatarUrl?:""
                    )
                )

                messageEditText.text.clear()
            }

            ReduxStore.subscribe {
                when (it) {
                    Action.ReceiveMessage ->
                        adapter.addMessage(it.associatedData as MessageResponse)
                    else -> {}
                }
            }
        }

        return view
    }
}
