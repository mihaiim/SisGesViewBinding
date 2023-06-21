package com.mihaiim.sisgesviewbinding.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.RowAdministrationBinding
import com.mihaiim.sisgesviewbinding.domain.model.Administration
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum

class AdministrationAdapter : RecyclerView.Adapter<AdministrationAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Administration>() {
        override fun areItemsTheSame(oldItem: Administration, newItem: Administration): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Administration, newItem: Administration): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowAdministrationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size

    fun submitList(list: List<Administration>) = differ.submitList(list)

    inner class ViewHolder(val binding: RowAdministrationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                binding.nameTv.maxLines = if (binding.nameTv.maxLines == 1) 100 else 1
            }
        }

        fun bind(item: Administration) {
            if (adapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.light_grey))
            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.white))
            }
            if (item.type == AdministrationEnum.ADD) {
                binding.typeIv.setImageDrawable(AppCompatResources
                    .getDrawable(binding.root.context, R.drawable.ic_arrow_in))
            } else {
                binding.typeIv.setImageDrawable(AppCompatResources
                    .getDrawable(binding.root.context, R.drawable.ic_arrow_out))
            }
            binding.codeTv.text = item.productCode
            binding.nameTv.text = item.productName
            binding.quantityTv.text = binding.root.resources.getString(
                R.string.pieces_short,
                item.quantity,
            )
            binding.dateTv.text = item.getFormattedDateTime()
        }
    }
}