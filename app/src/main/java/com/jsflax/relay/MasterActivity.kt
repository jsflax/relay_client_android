package com.jsflax.relay

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.jsflax.relay.core.*
import com.relay.data.MessageRequest

class MasterActivity : AppCompatActivity() {
    companion object {
        const val MasterReduxKey = "masterReduxKey"
    }

    fun dismissAvatarDialog() {
        (supportFragmentManager.findFragmentByTag("avatar_selection")
            as AvatarSelectionFragment).dismiss()
    }

    fun showAvatarDialog() {
        AvatarSelectionFragment().show(
            supportFragmentManager, "avatar_selection"
        )
    }

    fun addLogInFragment() {
        supportActionBar?.title = "log in"

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .add(R.id.content, UserLogInFragment(), "content")
            .commit()
    }

    fun addSignUpFragment() {
        supportActionBar?.title = "sign up"

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .add(R.id.content, UserSignUpFragment(), "content")
            .commit()
    }

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

    fun renderChannelTab() {
        supportActionBar?.title = "channels"

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
            .replace(R.id.content, ChannelFragment(), "content")
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        ReduxStore.unsubscribe(MasterReduxKey)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        ReduxStore.subscribe(MasterReduxKey, {
            when (it) {
                Action.NavigateToChannels -> renderChannelTab()
                Action.NavigateToUser -> renderUserTab()
                Action.NavigateToChat -> renderChatTab()
                Action.LogOut -> {}
                Action.BeginLogIn -> addLogInFragment()
                Action.BeginSignUp -> addSignUpFragment()
                Action.ExecuteLogIn, Action.ExecuteSignUp -> renderUserTab()
                Action.Subscribe -> {
                    renderChatTab()
                }
                Action.SendMessage -> {
                    ReduxStore.client.send(
                        Gson().toJson(it.associatedData as MessageRequest)
                    )
                }
                Action.CreateChannel -> {
                }
                Action.ShowAvatars -> showAvatarDialog()
                Action.SelectedAvatar -> dismissAvatarDialog()
                else -> {
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?

        setSupportActionBar(toolbar)

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
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
