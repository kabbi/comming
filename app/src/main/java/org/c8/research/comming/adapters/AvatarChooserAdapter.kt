package org.c8.research.comming.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import com.pawegio.kandroid.inflateLayout
import org.c8.research.comming.R

class AvatarChooserAdapter(
        private var dataset: Array<Avatar>,
        private var selectedPosition: Int,
        private var onItemSelected: (Avatar) -> Unit
) : RecyclerView.Adapter<AvatarChooserAdapter.ViewHolder>() {

    class ViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    data class Avatar(val id: String, val drawableResource: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.inflateLayout(R.layout.avatar_item, parent, false) as ImageView
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(dataset[position].drawableResource)
        holder.imageView.isSelected = position === selectedPosition
        holder.imageView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = position
            notifyItemChanged(selectedPosition)

            onItemSelected(dataset[position])
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}