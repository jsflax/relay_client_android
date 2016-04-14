package com.jsflax.relay.ui

import android.net.Uri
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest

import com.relay.data.MessageResponse

/**
 * @author jasonflax on 4/2/16.
 */
class MessageResponseViewHolder(val view: View) :
    RecyclerView.ViewHolder(view) {

    val message = view.findViewById(R.id.message) as? TextView

    fun bindMessage(response: SpannableStringBuilder) {
        message?.text = response
    }
}

class MessageAdapter(val inflater: LayoutInflater) :
    RecyclerView.Adapter<MessageResponseViewHolder>() {

    var messages: List<Pair<MessageResponse, SpannableStringBuilder>> = mutableListOf()

    fun bindMessageList(messageList:
                        List<Pair<MessageResponse, SpannableStringBuilder>>) {
        messages = messageList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = messages.size

    override fun onCreateViewHolder(parent: ViewGroup?,
                                    viewType: Int): MessageResponseViewHolder? {
        return MessageResponseViewHolder(
            inflater.inflate(R.layout.vh_message, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageResponseViewHolder?,
                                  position: Int) {
        holder?.bindMessage(messages[position].second)
    }
}

class MessageListViewHolder(val view: View) :
    RecyclerView.ViewHolder(view) {

    val avatar = view.findViewById(R.id.avatar) as SimpleDraweeView
    val messages = view.findViewById(R.id.messages) as RecyclerView

    init {
        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.stackFromEnd = true
        messages.layoutManager = layoutManager
        messages.adapter = MessageAdapter(LayoutInflater.from(view.context))
    }

    var previousItemViewType: Int = ChatAdapter.MessageTypeThem

    fun bindMessageList(messageList: MessageList,
                        itemViewType: Int) {
        if (previousItemViewType != itemViewType) {
            previousItemViewType = itemViewType

            ViewCompat.setLayoutDirection(
                view,
                itemViewType
            )

//            var children: List<View> = mutableListOf()
//
//            for (i in 0..(view as ViewGroup).childCount - 1) {
//                children += view.getChildAt(i)
//            }
//
//            view.removeAllViews()
//
//            for (child in children.reversed()) { view.addView(child) }
        }

        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(
                ImageRequest.fromUri(
                    Uri.parse(messageList.messages.first().first.avatarUrl)
                )
            )
            .setOldController(avatar.controller).build()

        avatar.controller = controller

        (messages.adapter as MessageAdapter).bindMessageList(
            messageList.messages
        )
    }
}

class MessageList(val userId: Int,
                  var messages: List<Pair<MessageResponse, SpannableStringBuilder>>)

class ChatAdapter(val layoutInflater: LayoutInflater,
                  val userId: Int) :
    RecyclerView.Adapter<MessageListViewHolder>() {
    companion object {
        internal const val MessageTypeThem: Int = 0
        internal const val MessageTypeMe: Int = 1
    }

    internal val messages: MutableList<MessageList> =
        mutableListOf()

    fun addMessage(messageResponse: Pair<MessageResponse, SpannableStringBuilder>) {
        if (messages.size > 0) {
            val lastMessages = messages.last()

            if (lastMessages.userId == messageResponse.first.userId) {
                lastMessages.messages += messageResponse
            } else {
                messages.add(
                    MessageList(
                        messageResponse.first.userId,
                        mutableListOf(messageResponse)
                    )
                )
            }
        } else {
            messages.add(
                MessageList(
                    messageResponse.first.userId,
                    mutableListOf(messageResponse)
                )
            )
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = this.messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].userId == userId) {
            MessageTypeMe
        } else {
            MessageTypeThem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?,
                                    viewType: Int): MessageListViewHolder? {
        return MessageListViewHolder(
            this.layoutInflater.inflate(
                R.layout.vh_message_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageListViewHolder?,
                                  position: Int) {
        holder?.bindMessageList(messages[position], getItemViewType(position))
    }
}

