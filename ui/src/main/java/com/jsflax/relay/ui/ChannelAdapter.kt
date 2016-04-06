package com.jsflax.relay.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.relay.data.Channel

/**
 * @author jasonflax on 4/2/16.
 */
class ChannelViewHolder(val view: View,
                        val onChannelClickListener: (Channel) -> Unit) :
    RecyclerView.ViewHolder(view) {

    val nameTextView by lazy { view.findViewById(R.id.name) as TextView }
    val descriptionTextView by lazy {
        view.findViewById(R.id.description) as TextView
    }

    fun bindChannel(channel: Channel) {
        view.setOnClickListener { onChannelClickListener.invoke(channel) }
        nameTextView.text = channel.name
        descriptionTextView.text = channel.description
    }
}

class ChannelAdapter(val layoutInflater: LayoutInflater,
                     val onChannelClickListener: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelViewHolder>() {

    val channels: MutableList<Channel> = mutableListOf()

    fun setChannels(channels: Array<Channel>) {
        this.channels.clear()
        this.channels.addAll(channels)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = this.channels.size

    override fun onCreateViewHolder(parent: ViewGroup?,
                                    viewType: Int): ChannelViewHolder? {
        return ChannelViewHolder(
            this.layoutInflater.inflate(R.layout.vh_channel, parent, false),
            onChannelClickListener
        )
    }

    override fun onBindViewHolder(holder: ChannelViewHolder?, position: Int) {
        holder?.bindChannel(channels[position])
    }
}
