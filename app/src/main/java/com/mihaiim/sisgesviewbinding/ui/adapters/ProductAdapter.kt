package com.mihaiim.sisgesviewbinding.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mihaiim.sisgesviewbinding.databinding.RowProductBinding
import com.mihaiim.sisgesviewbinding.domain.model.Product

class ProductAdapter(
    private val dataSet: Array<Product>,
    private val onItemClicked: (Product) -> Unit,
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        ) { onItemClicked(dataSet[it]) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.codeTv.text = dataSet[position].code
        holder.binding.nameTv.text = dataSet[position].name
    }

    override fun getItemCount() = dataSet.size

    inner class ViewHolder(
        val binding: RowProductBinding,
        onItemClicked: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}