package com.jsflax.relay

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.jsflax.relay.core.*
import com.relay.data.MessageRequest

class MasterActivity : AppCompatActivity() {
    companion object {
        // key meant to be used for subscribing
        // to redux actions
        const val MasterReduxKey = "masterReduxKey"
    }

    /**
     * Convenience method to dismiss an avatar selection dialog if it exists
     */
    fun dismissAvatarDialog() {
        (supportFragmentManager.findFragmentByTag("avatar_selection")
            as? AvatarSelectionFragment)?.dismiss()
    }

    /**
     * Convenience method to display an avatar selection dialog
     */
    fun showAvatarDialog() {
        AvatarSelectionFragment().show(
            supportFragmentManager, "avatar_selection"
        )
    }

    /**
     * Commit sign up fragment to stack
     */
    fun dismissCreateChannelDialog() {
        (supportFragmentManager.findFragmentByTag("create_channel")
            as? CreateChannelFragment)?.dismiss()
    }

    /**
     * Commit sign up fragment to stack
     */
    fun showCreateChannelDialog() {
        CreateChannelFragment().show(
            supportFragmentManager, "create_channel"
        )
    }

    /**
     * Commit log in fragment to stack
     */
    fun addLogInFragment() {
        supportActionBar?.title = "log in"

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .add(R.id.content, UserLogInFragment(), "content")
            .commit()
    }

    /**
     * Commit sign up fragment to stack
     */
    fun showSignUpFragment() {
        UserSignUpFragment().show(
            supportFragmentManager, "sign_up"
        )
    }

    /**
     * Render chat tab in replacement of current tab
     */
    fun renderChatTab() {
        supportActionBar?.title = ReduxStore.subscribedChannel?.name ?: "chat"
        val fragment = if (ReduxStore.subscribedChannel != null)
            ChatSomeFragment() else
            ChatNoneFragment()

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .disallowAddToBackStack()
            .replace(R.id.content, fragment, "content")
            .commit()
    }

    /**
     * Render user tab in replacement of current tab
     */
    fun renderUserTab() {
        supportActionBar?.title = "user"

        if (ReduxStore.user == null) {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                .replace(R.id.content, UserNoneFragment(), "content")
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                .replace(R.id.content, UserSomeFragment(), "content")
                .commitAllowingStateLoss()
        }
    }

    /**
     * Render channel tab in replacement of current tab
     */
    fun renderChannelTab() {
        supportActionBar?.title = "channels"

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .replace(R.id.content, ChannelFragment(), "content")
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set content view to top level layout
        setContentView(R.layout.activity_splash)

        // set the action bar
        val toolbar = findViewById(R.id.toolbar) as Toolbar?

        setSupportActionBar(toolbar)

        // reconnect websocket client if necessary
        ReduxStore.client.connect()

        // subscribe (or re-subscribe) to redux store,
        // reducing the actions
        ReduxStore.subscribe(MasterReduxKey, { action ->
            when (action) {
                Action.NavigateToChannels -> renderChannelTab()
                Action.NavigateToUser -> renderUserTab()
                Action.NavigateToChat -> renderChatTab()
                Action.LogOut -> {
                }
                Action.BeginLogIn -> addLogInFragment()
                Action.BeginSignUp -> showSignUpFragment()
                Action.ExecuteLogIn, Action.ExecuteSignUp -> renderUserTab()
                Action.Subscribe -> {
                    renderChatTab()
                }
                Action.SendMessage -> {
                    ReduxStore.client.send(
                        Gson().toJson(action.associatedData as MessageRequest)
                    )
                }
                Action.BeginCreateChannel -> {
                    showCreateChannelDialog()
                }
                Action.ExecuteCreateChannel -> {
                    dismissCreateChannelDialog()
                }
                Action.ShowAvatars -> showAvatarDialog()
                Action.SelectedAvatar -> dismissAvatarDialog()
                else -> {
                }
            }
        })


        // if this is our first time creating the activity,
        // reduce the current state of the application and render the
        // appropriate tab
        if (savedInstanceState == null) {
            when (ReduxStore.getState()) {
                State.NotSubscribedAndLoggedIn -> {
                    renderChannelTab()
                }
                State.NotSubscribedAndNotLoggedIn -> {
                    renderChannelTab()
                }
                State.SubscribedAndLoggedIn -> {
                    renderChatTab()
                }
                State.SubscribedAndNotLoggedIn -> {
                    renderChatTab()
                }
                else -> {
                }
            }
        }

        // set on click listeners for the respective tab buttons
        findViewById(R.id.nav_channels_button)?.setOnClickListener {
            ReduxStore.dispatch(Action.NavigateToChannels)
        }
        findViewById(R.id.nav_chat_button)?.setOnClickListener {
            ReduxStore.dispatch(Action.NavigateToChat)
        }
        findViewById(R.id.nav_user_button)?.setOnClickListener {
            ReduxStore.dispatch(Action.NavigateToUser)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_splash, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
