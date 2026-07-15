package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemSaleProductBinding
import com.example.freshcontroll.domain.model.SaleDetail

class SaleProductAdapter(private val onRemoveClick: (String) -> Unit) :
    ListAdapter<SaleDetail, SaleProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSaleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSaleProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SaleDetail) {
            // Asignación corregida según los IDs reales del XML
            binding.tvProductName.text = item.productName

            // Usando tvProductCalculation (antes tvQuantityPrice)
            binding.tvProductCalculation.text = "${item.quantity} x S/ ${item.unitPrice}"

            // Usando tvProductItemTotal (antes tvTotal)
            binding.tvProductItemTotal.text = "S/ ${item.quantity * item.unitPrice}"

            // Usando btnRemoveItem (antes btnRemove)
            binding.btnRemoveItem.setOnClickListener { onRemoveClick(item.productId) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SaleDetail>() {
        override fun areItemsTheSame(old: SaleDetail, new: SaleDetail) = old.productId == new.productId
        override fun areContentsTheSame(old: SaleDetail, new: SaleDetail) = old == new
    }
}