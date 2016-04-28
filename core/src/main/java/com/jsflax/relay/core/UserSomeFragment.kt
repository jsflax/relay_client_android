package com.jsflax.relay.core

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.relay.service.RelayService

/**
 * @author jasonflax on 4/4/16.
 */
class UserSomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        if (ReduxStore.user != null) {
            val user = ReduxStore.user

            if (view == null) {
                view = inflater?.inflate(
                    R.layout.fg_user_some, container, false
                )

                val username = (view?.findViewById(R.id.username) as TextView)
                val avatarImage =
                    (view?.findViewById(R.id.avatar_image) as SimpleDraweeView)

                view?.findViewById(
                    R.id.button_select_avatar
                )?.setOnClickListener {
                    ReduxStore.dispatch(Action.ShowAvatars)
                }

                username.text = user?.name?:""
                avatarImage.setImageURI(
                    Uri.parse("http://" + RelayService.config?.host + user?.avatarUrl)
                )
            }
        } else {
            ReduxStore.dispatch(Action.BeginSignUp)
        }

        return view
    }
}