package com.mihaiim.sisgesviewbinding.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mihaiim.sisgesviewbinding.databinding.RowProductPositionsBinding

class ProductPositionsAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<ProductPositionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowProductPositionsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textView.text = dataSet[position]
    }

    override fun getItemCount() = dataSet.size

    inner class ViewHolder(val binding: RowProductPositionsBinding) :
        RecyclerView.ViewHolder(binding.root)
}