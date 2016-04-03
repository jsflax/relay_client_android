package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jsflax.relay.ui.ChannelAdapter
import com.jsflax.relay.ui.R
import com.relay.service.Channels

/**
 * @author jasonflax on 4/2/16.
 */
class ChannelFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // see if view has previously been created
        var view =  super.onCreateView(inflater, container, savedInstanceState)
        // if not, inflate new view for channel fragment
        if (view == null) {
            view = inflater?.inflate(R.layout.view_fg_channel, container)
            // fetch list of channels from server
            async({ Channels.get() }) {
                // create new channel adapter for list of channels
                val adapter = ChannelAdapter(LayoutInflater.from(context))
                (view?.findViewById(R.id.channel_recycler_view) as?
                    RecyclerView)?.adapter = adapter
                // set channels, if null, then empty array
                adapter.setChannels(it.parcel?.toTypedArray()?:arrayOf())
            }
        }
        return view
    }
}
