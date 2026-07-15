package com.example.freshcontroll.presentation.inventory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.ItemProductBinding
import com.example.freshcontroll.domain.model.Product

class ProductAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context

            // Asignación de datos basada en tus IDs de XML
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = String.format("S/ %.2f / %s", product.price, product.unitType)
            binding.tvProductQuantity.text = "Cant: ${product.currentStock} ${product.unitType}"

            // Lógica de color y texto del badge de stock
            when {
                product.currentStock <= 0 -> {
                    binding.tvStockStatus.text = "Agotado"
                    binding.cvStockBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error))
                }
                product.currentStock <= product.minStock -> {
                    binding.tvStockStatus.text = "Stock Bajo"
                    binding.cvStockBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.naranja_alerta))
                }
                else -> {
                    binding.tvStockStatus.text = "En Stock"
                    binding.cvStockBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.exito))
                }
            }

            // Click listener principal para navegar al detalle
            binding.root.setOnClickListener {
                onItemClick(product.id)
            }

            // Click listener para el botón de editar (puedes definir aquí su acción específica)
            binding.btnEditProduct.setOnClickListener {
                // TODO: Acción de edición rápida si lo requieres, o navega al detalle
                onItemClick(product.id)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
}