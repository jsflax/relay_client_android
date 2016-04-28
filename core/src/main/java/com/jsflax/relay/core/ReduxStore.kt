package com.jsflax.relay.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.relay.data.Channel
import com.relay.data.Emoticon
import com.relay.data.MessageResponse
import com.relay.data.User
import com.relay.service.Emoticons
import com.relay.service.RelayService
import com.relay.service.base.WebSocketClient
import com.relay.service.toMap
import java.util.*
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.plusAssign

/**
 * Possible app states
 *
 * @param float mutable key for the state of the application
 */
enum class State(internal val float: Float) {
    /**
     * Base state, to be incremented
     */
    Open(0f),
    /**
     * User has not subscribed to any channels
     * and is not logged into an account
     */
    NotSubscribedAndNotLoggedIn(0.5f),
    /**
     * User has not subscribed to any channels
     * but is logged into an account
     */
    NotSubscribedAndLoggedIn(1.0f),
    /**
     * User has subscribed to a channel
     * but is not logged in (and as such, cannot send messages)
     */
    SubscribedAndNotLoggedIn(1.5f),
    /**
     * User has subscribed to a channel
     * and is logged in
     */
    SubscribedAndLoggedIn(2.0f);

    /**
     * Log out, decrementing state by half
     */
    fun logout(): State {
        return values().find {
            it.float == this.float - 0.5f
        } ?: Open
    }

    /**
     * Log in, incrementing state by half
     */
    fun login(): State {
        return values().find {
            it.float == this.float + 0.5f
        } ?: Open
    }

    /**
     * Hop to the next full state, e.g., not subscribed to subscribed
     */
    fun hop(): State {
        return values().find {
            it.float == this.float + 1f
        } ?: Open
    }
}

/**
 * Possible actions that can be taken throughout the application
 */
enum class Action {
    /**
     * Subscribe to a channel
     */
    Subscribe,
    /**
     * Begin channel creation process
     */
    BeginCreateChannel,
    /**
     * Actually create channel
     */
    ExecuteCreateChannel,
    /**
     * Begin the log in process
     */
    BeginLogIn,
    /**
     * Log in to an account
     */
    ExecuteLogIn,
    /**
     * Begin the sign up process
     */
    BeginSignUp,
    /**
     * Execute a requested sign up
     */
    ExecuteSignUp,
    /**
     * Execute a requested log out
     */
    LogOut,
    /**
     * Execute a message send
     */
    SendMessage,
    /**
     * Receive a message from the channel
     */
    ReceiveMessage,
    /**
     * Navigate to user tab
     */
    NavigateToUser,
    /**
     * Navigate to channels tab
     */
    NavigateToChannels,
    /**
     * Navigate to chat tab
     */
    NavigateToChat,
    /**
     * Show avatar selection
     */
    ShowAvatars,
    /**
     * Selected an avatar from the selection
     */
    SelectedAvatar,
    /**
     * Selected an emoticon from the selection
     */
    SelectedEmoticon;

    /**
     * Singular piece of data that can be associated with an action
     */
    var associatedData: Any? = null
}

private class ExpiryStorage(val expires: Long,
                            val data: String)

object ReduxStore {

    var subscribedChannel: Channel? = null
    var _user: User? = null
    var user: User?
        set(value) {
            sharedPreferences?.edit()?.putString(
                "user",
                Gson().toJson(value?.map)
            )?.apply()
            _user = value
        }
        get() {
            if (_user == null) {
                val serUser = sharedPreferences?.getString("user", "")
                if (!serUser.isNullOrEmpty()) {
                    _user = User(Gson().fromJson(serUser, mutableMapOf<String, Any?>().javaClass))
                }
            }
            return _user
        }

    val uuid: String by lazy {
        val serUuid = sharedPreferences?.getString("uuid", "")
        if (serUuid.isNullOrEmpty()) {
            val uuid = UUID.randomUUID().toString().replace("-", "");
            sharedPreferences?.edit()?.putString("uuid", uuid)?.apply()
            uuid
        } else {
            serUuid!!
        }
    }

    private var _emoticons: Array<Emoticon> = arrayOf()
    var emoticons: Array<Emoticon> = _emoticons
    get() {
        if (_emoticons.isEmpty()) {
            val serEmoticons = sharedPreferences?.getString("emotes", "")
            val restore = {
                async({ Emoticons.get() }) {
                    _emoticons = it.parcel?.toTypedArray()?:arrayOf()

                    sharedPreferences?.edit()?.putString(
                        "emotes",
                        Gson().toJson(
                            ExpiryStorage(
                                System.currentTimeMillis() + 604800000,
                                Gson().toJson(_emoticons)
                            )
                        )
                    )?.apply()
                }
            }
            if (serEmoticons.isNullOrEmpty()) {
                restore()
            } else {
                val stored = Gson().fromJson(
                    serEmoticons, ExpiryStorage::class.java
                )

                if (stored.expires < System.currentTimeMillis()) {
                    restore()
                }

                _emoticons = Gson().fromJson(
                    stored.data, Array<Emoticon>::class.java
                )
            }

            return _emoticons
        } else {
            return _emoticons
        }
    }

    private var state: State = State.NotSubscribedAndNotLoggedIn
    fun getState() = state

    private val binders: MutableMap<String, (Action) -> Unit> =
        mutableMapOf()

    private val sharedPreferences: SharedPreferences? by lazy {
        if (context == null) {
            throw IllegalAccessException("Must initialize redux with context")
        }

        context?.getSharedPreferences(
            "com.jsflax.relay",
            Context.MODE_PRIVATE
        )
    }

    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
    }

    fun reduce(action: Action) {
        when (action) {
        // can subscribe from any state
        // we must search for the current state to determine
        // what the new state is
            Action.Subscribe -> {
                subscribedChannel = action.associatedData as Channel
                when (state) {
                    State.NotSubscribedAndLoggedIn,
                    State.NotSubscribedAndNotLoggedIn -> state = state.hop()
                    else -> state
                }
            }
            Action.ExecuteCreateChannel -> when (state) {
                State.NotSubscribedAndLoggedIn,
                State.NotSubscribedAndNotLoggedIn -> state = state.hop()
                else -> state
            }
            // logging in or signing up increments the state to a
            // logged in state
            Action.ExecuteLogIn, Action.ExecuteSignUp -> {
                if (action.associatedData is User) {
                    user = action.associatedData as User
                }
                state = state.login()
            }
            else -> state
        }
    }

    private var lastKey: Int = 0

    fun subscribe(key: String, binder: (Action) -> Unit) {
        if (key.contains("[^a-zA-Z0-9]+".toRegex())) {
            throw IllegalArgumentException(
                "redux keys must be alphanumeric characters only"
            )
        }
        binders += key to binder
    }

    fun subscribe(binder: (Action) -> Unit) {
        binders += "$${lastKey++}" to binder
    }

    fun unsubscribe(key: String) {
        binders.remove(key)
    }

    /**
     * Send an action to everyone who has subscribed to the redux store.
     * Assign any associated data with the prescribed action.
     *
     * @param action action to send
     * @param associatedData anything that needs to be
     *                       associated with this action
     */
    fun dispatch(action: Action, associatedData: Any?) {
        action.associatedData = associatedData
        dispatch(action)
    }

    /**
     * Send an action to everyone who has subscribed to the redux store.
     *
     * @param action action to send
     */
    fun dispatch(action: Action) {
        // execute reduction, possibly mutating the current
        // state of the application
        reduce(action)

        binders.forEach { it.value.invoke(action) }
    }

    val client: WebSocketClient by lazy {
        WebSocketClient(
            "/chat/$uuid",
            MasterChatListener()
        )
    }

    private class MasterChatListener :
        WebSocketClient.Listener {
        val TAG = "WSS"

        override fun onConnect() {
            Log.d(TAG, "Connected!")
        }

        override fun onMessage(message: String) {
            Log.d(TAG, String.format("Got string message! %s", message))
            ReduxStore.dispatch(
                Action.ReceiveMessage,
                MessageResponse(
                    toMap(JsonParser().parse(message).asJsonObject),
                    RelayService.config?.host ?: ""
                )
            )
        }

        override fun onMessage(data: ByteArray) {
            Log.d(TAG, "Got binary message! $data")
        }

        override fun onDisconnect(code: Int, reason: String?) {
            Log.d(TAG, "Disconnected! Code: $code Reason: $reason")
        }

        override fun onError(error: Exception) {
            Log.e(TAG, "Error!", error)
        }
    }
}
