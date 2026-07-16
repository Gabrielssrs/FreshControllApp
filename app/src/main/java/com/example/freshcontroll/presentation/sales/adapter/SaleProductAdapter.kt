package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemSaleProductBinding
import com.example.freshcontroll.domain.model.SaleDetail

class SaleProductAdapter(
    private val onQuantityChange: (String, Double) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<SaleDetail, SaleProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSaleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSaleProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SaleDetail) {
            binding.tvProductName.text = item.productName
            binding.tvProductCalculation.text = "S/ ${String.format("%.2f", item.unitPrice)}"
            binding.tvQuantity.text = "${item.quantity}"
            binding.tvProductItemTotal.text = "S/ ${String.format("%.2f", item.totalPrice)}"

            binding.btnDecreaseQuantity.setOnClickListener {
                if (item.quantity > 1) {
                    onQuantityChange(item.productId, item.quantity - 1)
                }
            }

            binding.btnIncreaseQuantity.setOnClickListener {
                onQuantityChange(item.productId, item.quantity + 1)
            }

            binding.btnRemoveItem.setOnClickListener { 
                onRemoveClick(item.productId) 
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SaleDetail>() {
        override fun areItemsTheSame(old: SaleDetail, new: SaleDetail) = old.productId == new.productId
        override fun areContentsTheSame(old: SaleDetail, new: SaleDetail) = old == new
    }
}
