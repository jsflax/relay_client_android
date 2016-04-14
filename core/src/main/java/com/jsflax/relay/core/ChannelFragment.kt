package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jsflax.relay.ui.ChannelAdapter
import com.relay.data.Channel
import com.relay.data.SimpleUuid
import com.relay.service.Channels

/**
 * @author jasonflax on 4/2/16.
 */
class ChannelFragment : Fragment() {
    // create new channel adapter for list of channels
    val adapter by lazy {
        ChannelAdapter(
            LayoutInflater.from(context), { channel ->
            async({
                Channels.subscribe(
                    channel.id,
                    SimpleUuid(ReduxStore.uuid)
                )
            }) {
                // if call was successful, dispatch action to Redux,
                // with associated data
                // else, display error
                if (it.status == 200) {
                    ReduxStore.dispatch(
                        Action.Subscribe, channel
                    )
                } else {
                    showErrorDialog(
                        context,
                        "error",
                        it.message
                    )
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putSerializable("channels", adapter.channels.toTypedArray())
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // see if view has previously been created
        var view = super.onCreateView(inflater, container, savedInstanceState)
        // if not, inflate new view for channel fragment
        if (view == null) {
            view = inflater?.inflate(R.layout.fg_channel, container, false)
            val recyclerView =
                (view?.findViewById(R.id.channel_recycler_view) as?
                    RecyclerView)

            recyclerView?.layoutManager = LinearLayoutManager(context)

            recyclerView?.adapter = adapter

            view?.findViewById(R.id.fab)?.setOnClickListener {
                ReduxStore.dispatch(Action.BeginCreateChannel)
            }

            if (savedInstanceState == null) {
                // fetch list of channels from server
                async({ Channels.get() }) {
                    // set channels, if null, then empty array
                    adapter.setChannels(it.parcel?.toTypedArray() ?: arrayOf())
                }
            } else {
                adapter.setChannels(
                    savedInstanceState.getSerializable("channels")
                        as Array<Channel>
                )
            }
        }

        return view
    }
}
