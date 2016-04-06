package com.jsflax.relay.core

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.relay.data.Asset
import com.relay.data.UserCreateRequest
import com.relay.data.UserLoginRequest
import com.relay.service.Users

/**
 * @author jasonflax on 4/3/16.
 */
class UserLogInFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        if (view == null) {
            // inflate sign up layout
            view = inflater?.inflate(
                R.layout.fg_user_log_in, container, false
            )
            // set on click listener to avatar button
            view?.findViewById(R.id.button_select_avatar)?.setOnClickListener {
                // dispatch action to show avatars
                ReduxStore.dispatch(Action.ShowAvatars)
            }

            val usernameField =
                view?.findViewById(R.id.log_in_username) as? EditText
            val passwordField =
                view?.findViewById(R.id.log_in_password) as? EditText

            // set on click listener to sign up button
            view?.findViewById(R.id.button_log_in)?.setOnClickListener {
                // if password length is less than 6, supply error
                // else if username length is 0, supply error
                // else, attempt user creation request
                if (passwordField?.text?.length ?: 0 <= 5) {
                    showErrorDialog(
                        context,
                        "error",
                        "password must be at least 6 characters"
                    )
                } else if (usernameField?.text?.isEmpty() ?: true) {
                    showErrorDialog(
                        context,
                        "error",
                        "must enter a username"
                    )
                } else {
                    // display loading spinner
                    val dialog = getIndeterminateDialog(context)
                    dialog.show()

                    // PUT sign up request to server
                    async({
                        Users.login(
                            UserLoginRequest(
                                password = passwordField?.text.toString(),
                                name = usernameField?.text.toString()
                            )
                        )
                    }) {
                        // dismiss dialog
                        dialog.dismiss()
                        // if call was successful, dispatch action to Redux,
                        // with associated data
                        // else, display error
                        if (it.status == 200) {
                            ReduxStore.dispatch(
                                Action.ExecuteLogIn, it.parcel!!
                            )
                        } else {
                            showErrorDialog(
                                context,
                                "error",
                                it.message
                            )
                        }
                    }
                }
            }

            // subscribe to ReduxStore
            // if the user selects an avatar, it will be dispatched here
            ReduxStore.subscribe {
                when (it) {
                    Action.SelectedAvatar -> {
                        // coerce associated data to asset chosen
                        val asset = (it.associatedData as Asset)

                        // render asset
                        (view?.findViewById(R.id.avatar_image) as?
                            SimpleDraweeView)?.setImageURI(
                            Uri.parse(asset.host + asset.path)
                        )

                        // allow for avatar selection by clicking
                        // on the rendered image
                        view?.findViewById(
                            R.id.avatar_image
                        )?.setOnClickListener {
                            ReduxStore.dispatch(Action.ShowAvatars)
                        }

                        // render name of asset
                        (view?.findViewById(R.id.asset_name)
                            as? TextView)?.text = asset.name
                    }
                    else -> {}
                }
            }
        }

        return view
    }
}
