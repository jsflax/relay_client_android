package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.relay.data.ChannelCreateRequest
import com.relay.service.Channels

/**
 * @author jasonflax on 4/12/16.
 */

/**
 * @author jasonflax on 4/2/16.
 */
class CreateChannelFragment : DialogFragment() {
    init {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog)

    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // see if view has previously been created
        var view = super.onCreateView(inflater, container, savedInstanceState)
        // if not, inflate new view for channel fragment
        if (view == null) {
            view = inflater?.inflate(R.layout.fg_create_channel, container, false)

            val name = view?.findViewById(R.id.channel_name) as EditText
            val description =
                view?.findViewById(R.id.channel_description) as EditText

            view?.findViewById(R.id.create_channel)?.setOnClickListener {
                if (name.text.isNotEmpty()) {
                    async({
                        Channels.create(
                            ChannelCreateRequest(
                                ReduxStore.user?.token?:"",
                                name.text.toString(),
                                description.text.toString()
                            )
                        )
                    }) {
                        ReduxStore.dispatch(
                            Action.ExecuteCreateChannel, it.parcel
                        )
                    }
                }
            }
        }

        return view
    }
}
