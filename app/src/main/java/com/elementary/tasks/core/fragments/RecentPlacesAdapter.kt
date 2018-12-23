package com.elementary.tasks.core.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.list_item_map_place.view.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RecentPlacesAdapter : RecyclerView.Adapter<RecentPlacesAdapter.ViewHolder>() {

    private val mData = mutableListOf<Place>()
    var actionsListener: ActionsListener<Place>? = null

    var data: List<Place>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Place) {
            itemView.textView.text = item.name
            loadMarker(itemView.markerImage, item.marker)

            val dmy = TimeUtil.getPlaceDateTimeFromGmt(item.dateTime)
            itemView.dayView.text = dmy.day
            itemView.monthYearView.text = "${dmy.month}\n${dmy.year}"
        }

        init {
            itemView.itemCard.setOnClickListener { view ->
                actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
        }
    }

    fun getItem(position: Int): Place {
        return mData[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_map_place, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun loadMarker(view: ImageView, color: Int) {
        view.setImageResource(ThemeUtil.getMarkerStyle(color))
    }
}