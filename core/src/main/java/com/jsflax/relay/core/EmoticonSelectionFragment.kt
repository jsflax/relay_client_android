package com.jsflax.relay.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jsflax.relay.ui.EmoticonAdapter
import com.relay.data.Emoticon

/**
 * @author jasonflax on 4/3/16.
 */
class EmoticonSelectionFragment : Fragment() {
    val adapter by lazy {
        EmoticonAdapter(
            LayoutInflater.from(context), {
            ReduxStore.dispatch(Action.SelectedEmoticon, it)
        })
    }

    fun applyEmoticons(emoticons: Array<Emoticon>) {
        activity.runOnUiThread {
            adapter.setEmoticons(emoticons)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view == null) {
            view = inflater?.inflate(
                R.layout.fg_emoticon_selection, container, false
            )
            val recyclerView =
                view?.findViewById(R.id.emoticon_recycler_view) as? RecyclerView

            recyclerView?.layoutManager = LinearLayoutManager(context)
            recyclerView?.adapter = adapter

            (recyclerView?.adapter as EmoticonAdapter).setEmoticons(
                ReduxStore.emoticons
            )
        }

        return view
    }
}
