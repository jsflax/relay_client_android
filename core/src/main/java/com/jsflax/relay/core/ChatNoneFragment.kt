package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author jasonflax on 4/4/16.
 */
class ChatNoneFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view == null) {
            view = inflater?.inflate(
                R.layout.fg_chat_none, container, false
            )

            view?.findViewById(R.id.button_see_channels)?.setOnClickListener {
                ReduxStore.dispatch(Action.NavigateToChannels)
            }
        }

        return view
    }
}
