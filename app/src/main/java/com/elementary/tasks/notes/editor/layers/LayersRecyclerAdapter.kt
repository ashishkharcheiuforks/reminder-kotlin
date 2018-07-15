package com.elementary.tasks.notes.editor.layers

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.drawing.Background
import com.elementary.tasks.core.drawing.Drawing
import com.elementary.tasks.core.drawing.Image
import com.elementary.tasks.core.drawing.Text
import com.elementary.tasks.core.interfaces.Observer
import com.elementary.tasks.databinding.ListItemLayerBinding

import java.util.Collections

import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright 2016 Nazar Suhovich
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
class LayersRecyclerAdapter(private val mContext: Context, private val mDataList: MutableList<Drawing>, private val onStartDragListener: OnStartDragListener, private val mCallback: AdapterCallback?) : RecyclerView.Adapter<LayersRecyclerAdapter.ViewHolder>(), Observer {
    private var index: Int = 0

    fun setIndex(index: Int) {
        this.index = index
        notifyDataSetChanged()
    }

    fun getIndex(): Int {
        return index
    }

    internal fun onItemDismiss(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, mDataList.size)
        mCallback?.onItemRemoved(position)
    }

    internal fun onItemMove(from: Int, to: Int) {
        Collections.swap(mDataList, from, to)
        notifyItemMoved(from, to)
        mCallback?.onChanged()
        notifyDataSetChanged()
    }

    override fun setUpdate(o: Any) {
        if (o is Int) {
            this.index = o - 1
        }
        notifyDataSetChanged()
        mCallback?.onItemAdded()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var binding: ListItemLayerBinding? = null

        init {
            binding = DataBindingUtil.bind(v)
            v.setOnClickListener { view ->
                mCallback?.onItemSelect(adapterPosition)
            }
            v.setOnLongClickListener { view ->
                onStartDragListener.onStartDrag(this)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayersRecyclerAdapter.ViewHolder {
        return ViewHolder(ListItemLayerBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: LayersRecyclerAdapter.ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.binding!!.layerName.text = getName(item)
        holder.binding!!.layerView.drawing = item
        if (position == index) {
            holder.binding!!.selectionView.setBackgroundResource(R.color.redPrimary)
        } else {
            holder.binding!!.selectionView.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun getName(drawing: Drawing): String {
        return if (drawing is Background) {
            mContext.getString(R.string.background)
        } else if (drawing is Image) {
            mContext.getString(R.string.image)
        } else if (drawing is Text) {
            drawing.text
        } else {
            mContext.getString(R.string.figure)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    interface AdapterCallback {
        fun onChanged()

        fun onItemSelect(position: Int)

        fun onItemRemoved(position: Int)

        fun onItemAdded()
    }
}