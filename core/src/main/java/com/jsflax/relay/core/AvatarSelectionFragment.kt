package com.jsflax.relay.core

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridView
import com.jsflax.relay.ui.AvatarAdapter
import com.relay.service.Assets

/**
 * @author jasonflax on 4/3/16.
 */
class AvatarSelectionFragment : DialogFragment() {
    /**
     * Overridden to set display width of the dialog, which is too small
     * natively.
     *
     * @link {DialogFragment#onResume}
     */
    override fun onResume() {
        super.onResume()
        // fetch window service
        val wm =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // fetch the display
        val display = wm.defaultDisplay
        // allocated a new point to sink the size of the screen
        val point = Point()
        display.getSize(point)
        // set the height and width of the dialog,
        // width being 90% of the screen width
        dialog.window.setLayout(
            (point.x * .9f).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view == null) {
            view = inflater?.inflate(
                R.layout.view_avatar_selection, container, false
            )
            val grid = view?.findViewById(R.id.avatar_grid) as? GridView

            // set number of columns
            grid?.numColumns = 3

            // fetch list of avatars and set up
            // the grid
            async({ Assets.avatars() }) {
                // if we've received avatars,
                // adapt the data for the grid
                if (it.parcel?.isNotEmpty()?:false) {
                    grid?.adapter = AvatarAdapter(
                        it.parcel!!,
                        { ReduxStore.dispatch(Action.SelectedAvatar, it) },
                        LayoutInflater.from(context)
                    )
                }
            }
        }

        return view
    }
}
