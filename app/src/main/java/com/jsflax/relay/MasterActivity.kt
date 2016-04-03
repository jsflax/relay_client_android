package com.jsflax.relay

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.jsflax.relay.core.Action
import com.jsflax.relay.core.ChannelFragment
import com.jsflax.relay.core.ReduxStore
import com.jsflax.relay.core.State

class MasterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?

        setSupportActionBar(toolbar)

        when (ReduxStore.getState()) {
            State.NotSubscribedAndLoggedIn -> {

            }
            State.NotSubscribedAndNotLoggedIn -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, ChannelFragment(), "content")
                    .commit()
            }
            State.SubscribedAndLoggedIn -> {}
            State.SubscribedAndNotLoggedIn -> {}
            else -> {}
        }

        ReduxStore.subscribe {
            when (it) {
                Action.CheckChannels -> {}
                Action.CheckUser -> {}
                Action.LogIn -> {}
                Action.LogOut -> {}
                Action.SignUp -> {}
                Action.Subscribe -> {}
                Action.SendMessage -> {}
                Action.CreateChannel -> {}
            }
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
