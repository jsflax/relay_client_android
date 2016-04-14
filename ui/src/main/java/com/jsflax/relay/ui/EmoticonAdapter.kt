package com.jsflax.relay.ui

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.relay.data.Emoticon

/**
 * @author jasonflax on 4/5/16.
 */
/**
 * @author jasonflax on 4/2/16.
 */
class EmoticonViewHolder(val view: View,
                         val onEmoticonClickListener: (Emoticon) -> Unit) :
    RecyclerView.ViewHolder(view) {

    val nameTextView by lazy { view.findViewById(R.id.emoticon_name) as TextView }
    val emoticonIcon by lazy {
        view.findViewById(R.id.emoticon_icon) as SimpleDraweeView
    }

    fun bindEmoticon(emoticon: Emoticon) {
        view.setOnClickListener { onEmoticonClickListener.invoke(emoticon) }

        nameTextView.text = emoticon.shortcut

        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(Uri.parse(emoticon.url))
            .setAutoPlayAnimations(true)
            .build()

        emoticonIcon.controller = controller
    }
}

/**
 * Adapter with the purpose of adapting a collection of emoticons into a list.
 *
 * @param layoutInflater system service to inflate xml layout files
 * @param onEmoticonClickListener callback for when an emoticon is tapped
 */
class EmoticonAdapter(val layoutInflater: LayoutInflater,
                      val onEmoticonClickListener: (Emoticon) -> Unit) :
    RecyclerView.Adapter<EmoticonViewHolder>() {

    /**
     * Mutable collection of emoticons
     */
    val emoticons: MutableList<Emoticon> = mutableListOf()

    /**
     * Assign the emoticons and notify the adapter that the data has changed
     */
    fun setEmoticons(emoticons: Array<Emoticon>) {
        this.emoticons.clear()
        this.emoticons.addAll(emoticons)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = this.emoticons.size

    override fun onCreateViewHolder(parent: ViewGroup?,
                                    viewType: Int): EmoticonViewHolder? {
        // instantiate new VH for emoticons,
        // passing along the callback
        return EmoticonViewHolder(
            this.layoutInflater.inflate(R.layout.vh_emoticon, parent, false),
            onEmoticonClickListener
        )
    }

    override fun onBindViewHolder(holder: EmoticonViewHolder?, position: Int) {
        holder?.bindEmoticon(emoticons[position])
    }
}
