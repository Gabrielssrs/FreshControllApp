package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemSoldProductBinding
import com.example.freshcontroll.domain.model.SaleDetail

class SoldProductAdapter : ListAdapter<SaleDetail, SoldProductAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSoldProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSoldProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SaleDetail) {
            // Asignación corregida según los IDs reales del XML
            binding.tvProductName.text = item.productName

            // Usando tvProductCalculation (antes tvQuantityPrice)
            binding.tvProductCalculation.text = "${item.quantity} x S/ ${item.unitPrice}"

            // Usando tvProductItemTotal (antes tvTotal)
            binding.tvProductItemTotal.text = "S/ ${item.quantity * item.unitPrice}"
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<SaleDetail>() {
        override fun areItemsTheSame(o: SaleDetail, n: SaleDetail) = o.productId == n.productId
        override fun areContentsTheSame(o: SaleDetail, n: SaleDetail) = o == n
    }
}