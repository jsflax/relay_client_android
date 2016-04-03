package com.jsflax.relay.core

import android.os.AsyncTask
import com.relay.service.base.Payload

/**
 * @author jasonflax on 4/3/16.
 */
class NetworkFetcher<A>(val task: () -> Payload<A>,
                        val courier: (Payload<A>) -> Unit):
    AsyncTask<Any, Any, Payload<A>>() {

    override fun doInBackground(vararg params: Any?): Payload<A> {
        return task.invoke()
    }

    override fun onPostExecute(result: Payload<A>) {
        super.onPostExecute(result)

        courier.invoke(result)
    }
}

fun <A> async(task: () -> Payload<A>, result: (Payload<A>) -> Unit) =
    NetworkFetcher(task, result).execute()
