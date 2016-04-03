package com.jsflax.relay.core

import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.plusAssign


enum class State(internal val float: Float) {
    Open(0f),
    NotSubscribedAndNotLoggedIn(0.5f),
    NotSubscribedAndLoggedIn(1.0f),
    SubscribedAndNotLoggedIn(1.5f),
    SubscribedAndLoggedIn(2.0f);

    fun login(): State {
        return values().find {
            it.float == this.float + 0.5f
        } ?: Open
    }

    fun hop(): State {
        return values().find {
            it.float == this.float + 1f
        } ?: Open
    }
}

enum class Action {
    Subscribe,
    CreateChannel,
    LogIn,
    SignUp,
    LogOut,
    SendMessage,
    CheckUser,
    CheckChannels
}

object ReduxStore {
    var subscribedChannel: Int? = null
    private var state: State = State.Open
    fun getState() = state

    private val binders: MutableList<(Action) -> Unit> = mutableListOf()

    fun reduce(action: Action) {
        when (action) {
            // can subscribe from any state
            // we must search for the current state to determine
            // what the new state is
            Action.Subscribe ->
                when (state) {
                    State.NotSubscribedAndLoggedIn,
                    State.NotSubscribedAndLoggedIn -> state = state.hop()
                    else -> state
                }
            // creating a channel is essentially the same as subscribing
            Action.CreateChannel -> reduce(Action.Subscribe)
            // logging in or signing up increments the state to a
            // logged in state
            Action.LogIn, Action.SignUp -> state = state.login()
            else -> state
        }
    }

    fun subscribe(binder: (Action) -> Unit) {
        binders += binder
    }

    fun dispatch(action: Action) {
        reduce(action)
        binders.forEach { it.invoke(action) }
    }
}
