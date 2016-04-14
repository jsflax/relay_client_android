package com.jsflax.relay.ui

import android.content.Context
import android.database.DataSetObserver
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.relay.data.Asset

/**
 * @author jasonflax on 4/3/16.
 */
class AvatarViewHolder(context: Context,
                       attrs: AttributeSet):
    LinearLayout(context, attrs) {

    fun bindAsset(asset: Asset, onAvatarClickListener: (Asset) -> Unit) {
        val draweeView = (findViewById(R.id.avatar_image) as? SimpleDraweeView)
        val imageUri = Uri.parse(asset.host + asset.path)

        val request = ImageRequest.fromUri(imageUri)

        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(draweeView?.controller).build()

        draweeView?.controller = controller

        draweeView?.setOnClickListener {
            onAvatarClickListener.invoke(asset)
        }

        draweeView?.setOnLongClickListener {
            Toast.makeText(context, asset.name, Toast.LENGTH_SHORT).show()
            false
        }
    }
}

class AvatarAdapter(val assets: List<Asset>,
                    val onAvatarClickListener: (Asset) -> Unit,
                    val inflater: LayoutInflater): ListAdapter {
    override fun isEmpty(): Boolean = false

    override fun hasStableIds(): Boolean = false

    override fun getItemId(position: Int): Long =
        assets[position].hashCode().toLong()

    override fun areAllItemsEnabled(): Boolean = true

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getCount(): Int = assets.size

    override fun registerDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItem(position: Int): Any? = assets[position]

    override fun getViewTypeCount(): Int = 0

    override fun isEnabled(position: Int): Boolean = true

    override fun getView(position: Int,
                         cView: View?,
                         parent: ViewGroup?): View? {
        var convertView = cView
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.vh_avatar, parent, false)
        }

        (convertView as AvatarViewHolder).bindAsset(
            assets[position],
            onAvatarClickListener
        )

        return convertView
    }
}
