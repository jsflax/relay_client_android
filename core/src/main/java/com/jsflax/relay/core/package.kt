package com.jsflax.relay.core

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.FrameLayout
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

fun getIndeterminateDialog(context: Context): Dialog {
    val dialog = Dialog(context, R.style.CustomDialog);
    dialog.setCancelable(false);
    dialog.addContentView(
        View.inflate(
            context, R.layout.view_progressbar_indeterminate, null
        ),
        FrameLayout.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.margin25),
            context.resources.getDimensionPixelSize(R.dimen.margin25)
        )
    );

    return dialog
}

fun showErrorDialog(context: Context, title: String, message: String) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            android.R.string.ok,
            { dialog: DialogInterface, which: Int -> }
        )
        .show()
}
