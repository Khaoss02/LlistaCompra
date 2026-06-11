package com.guillem.repeticiollistacompra

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.guillem.repeticiollistacompra.databinding.ItemProductBinding
import com.guillem.repeticiollistacompra.databinding.ItemCategoryHeaderBinding

class ProductAdapter(
    private var allProducts: List<Product>,
    private var categories: List<Category>,
    private val onCheckedChange: (Product, Boolean) -> Unit,
    private val onDelete: (Product) -> Unit,
    private val onEdit: (Product) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private var adapterItems: List<Any> = emptyList()
    private var showCompleted: Boolean = true
    private val collapsedCategories = mutableSetOf<String>()

    init {
        applyFilterAndGrouping()
    }

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)
    class HeaderViewHolder(val binding: ItemCategoryHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (adapterItems[position] is Category) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemCategoryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ProductViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = adapterItems[position]

        if (holder is HeaderViewHolder && item is Category) {
            holder.binding.tvHeaderName.text = item.name
            try {
                holder.binding.tvHeaderName.setTextColor(item.color.toColorInt())
            } catch (_: Exception) {
                holder.binding.tvHeaderName.setTextColor(Color.BLACK)
            }
            
            // Icono de expansión/contracción personalizado
            // Nota: Se usan recursos de Android por defecto si los personalizados no están
            val iconRes = if (collapsedCategories.contains(item.id)) {
                android.R.drawable.arrow_down_float
            } else {
                android.R.drawable.arrow_up_float
            }
            holder.binding.tvHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0)

            holder.binding.root.setOnClickListener {
                if (collapsedCategories.contains(item.id)) {
                    collapsedCategories.remove(item.id)
                } else {
                    collapsedCategories.add(item.id)
                }
                applyFilterAndGrouping()
            }
        }
        else if (holder is ProductViewHolder && item is Product) {
            val category = categories.find { it.id == item.categoryId }

            holder.binding.tvProductName.text = if (item.quantity.isNotEmpty()) {
                "${item.name} (${item.quantity})"
            } else {
                item.name
            }

            if (item.completed) {
                holder.binding.tvProductName.paintFlags = holder.binding.tvProductName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.binding.root.alpha = 0.5f
            } else {
                holder.binding.tvProductName.paintFlags = holder.binding.tvProductName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.binding.root.alpha = 1.0f
            }

            category?.let {
                try {
                    holder.binding.tvProductName.setTextColor(it.color.toColorInt())
                } catch (_: Exception) {
                    holder.binding.tvProductName.setTextColor(Color.BLACK)
                }
            }

            holder.binding.cbCompleted.setOnCheckedChangeListener(null)
            holder.binding.cbCompleted.isChecked = item.completed
            holder.binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(item, isChecked)
            }

            holder.binding.btnDelete.setOnClickListener { onDelete(item) }
            holder.binding.root.setOnClickListener { onEdit(item) }
        }
    }

    override fun getItemCount(): Int = adapterItems.size

    fun updateData(newList: List<Product>, newCategories: List<Category> = categories) {
        allProducts = newList
        categories = newCategories
        applyFilterAndGrouping()
    }

    fun toggleVisibility() {
        showCompleted = !showCompleted
        applyFilterAndGrouping()
    }

    private fun applyFilterAndGrouping() {
        val filtered = if (showCompleted) allProducts else allProducts.filter { item -> !item.completed }
        val temporaryList = mutableListOf<Any>()
        val grouped = filtered.groupBy { it.categoryId }

        val sortedCategories = categories.sortedBy { it.name }

        sortedCategories.forEach { category ->
            val productsInCategory = grouped[category.id]

            if (!productsInCategory.isNullOrEmpty()) {
                temporaryList.add(category)

                if (!collapsedCategories.contains(category.id)) {
                    val sortedProducts = productsInCategory.sortedWith(
                        compareBy<Product> { it.completed }.thenBy { it.name }
                    )
                    temporaryList.addAll(sortedProducts)
                }
            }
        }

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = adapterItems.size
            override fun getNewListSize(): Int = temporaryList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = adapterItems[oldItemPosition]
                val newItem = temporaryList[newItemPosition]
                return if (oldItem is Category && newItem is Category) {
                    oldItem.id == newItem.id
                } else if (oldItem is Product && newItem is Product) {
                    oldItem.id == newItem.id
                } else {
                    false
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return adapterItems[oldItemPosition] == temporaryList[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        adapterItems = temporaryList
        diffResult.dispatchUpdatesTo(this)
    }
}
