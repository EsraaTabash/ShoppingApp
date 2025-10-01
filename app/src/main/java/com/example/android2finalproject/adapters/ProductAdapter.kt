package com.example.android2finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android2finalproject.R
import com.esraa.shoppingapp.data.model.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val onProductClick: (Product) -> Unit   // callback -> fragment رح يستخدمه
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val type: TextView = itemView.findViewById(R.id.item_type)
        val img: ImageView = itemView.findViewById(R.id.item_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.pName
        holder.price.text = product.pPrice.toString()
        holder.type.text = product.pType
        Glide.with(holder.img.context).load(product.imageUri).into(holder.img)

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}
