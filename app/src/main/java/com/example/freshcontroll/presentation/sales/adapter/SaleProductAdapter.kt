package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.ItemSaleProductBinding
import com.example.freshcontroll.presentation.sales.model.CartItemUiModel

class SaleProductAdapter(
    private val onQuantityChange: (String, Double) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<CartItemUiModel, SaleProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSaleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSaleProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItemUiModel) {
            val detail = item.detail
            binding.tvProductName.text = detail.productName
            binding.tvProductCalculation.text = "S/ ${String.format("%.2f", detail.unitPrice)}"
            binding.tvQuantity.text = "${detail.quantity}"
            binding.tvProductItemTotal.text = "S/ ${String.format("%.2f", detail.totalPrice)}"

            // Lógica de botones de cantidad
            binding.btnDecreaseQuantity.setOnClickListener {
                if (detail.quantity > 1) {
                    onQuantityChange(detail.productId, detail.quantity - 1)
                }
            }

            // Desactivar botón + si se alcanza el stock máximo
            binding.btnIncreaseQuantity.isEnabled = !item.isStockLimitReached
            binding.btnIncreaseQuantity.alpha = if (item.isStockLimitReached) 0.5f else 1.0f
            binding.tvStockWarning.visibility = if (item.isStockLimitReached) View.VISIBLE else View.GONE
            
            binding.btnIncreaseQuantity.setOnClickListener {
                if (!item.isStockLimitReached) {
                    onQuantityChange(detail.productId, detail.quantity + 1)
                }
            }

            binding.btnRemoveItem.setOnClickListener { 
                onRemoveClick(detail.productId) 
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItemUiModel>() {
        override fun areItemsTheSame(old: CartItemUiModel, new: CartItemUiModel) = old.detail.productId == new.detail.productId
        override fun areContentsTheSame(old: CartItemUiModel, new: CartItemUiModel) = old == new
    }
}
