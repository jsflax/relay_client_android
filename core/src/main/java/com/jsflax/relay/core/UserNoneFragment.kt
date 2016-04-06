package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author jasonflax on 4/3/16.
 */
class UserNoneFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // see if view has previously been created
        var view =  super.onCreateView(inflater, container, savedInstanceState)
        // if not, inflate new view for channel fragment
        if (view == null) {
            view = inflater?.inflate(
                R.layout.fg_user_none, container, false
            )
            view?.findViewById(R.id.log_in)?.setOnClickListener {
                ReduxStore.dispatch(Action.BeginLogIn)
            }
            view?.findViewById(R.id.sign_up)?.setOnClickListener {
                ReduxStore.dispatch(Action.BeginSignUp)
            }
        }
        return view
    }
}
