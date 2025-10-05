package com.example.android2finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android2finalproject.R
import com.example.android2finalproject.models.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val onProductClick: (Product) -> Unit,   // pass this 3 funs from
    private val onAddToCart: (Product) -> Unit,      // fragments to adapter in events time
    private val onMapClick: (Product) -> Unit        // open external map app (geo:)
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    //value for badge count for each prod
    private val counts = hashMapOf<String, Int>()
    //flag to show map icon
    private val showMap = hashSetOf<String>()

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val type: TextView = itemView.findViewById(R.id.item_type)
        val img: ImageView = itemView.findViewById(R.id.item_iv)
        val rating: TextView  = itemView.findViewById(R.id.item_rating)
        val description: TextView  = itemView.findViewById(R.id.item_description)
        // right icons
        val addToCartIv: ImageView = itemView.findViewById(R.id.addToCartIv)
        val cartBadgeTv: TextView  = itemView.findViewById(R.id.cartBadgeTv)
        val mapIvProduct: ImageView = itemView.findViewById(R.id.mapIvProduct)
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
        holder.description.text = product.pDescription
        holder.rating.text = "★ ${product.pRating}"

        Glide.with(holder.img.context)
            .load(product.imageUri)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.img)

        // when click on the product card  -> call onProductClick
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }

        //update badge
        val count = counts[product.id] ?: 0
        if (count > 0) {
            holder.cartBadgeTv.visibility = View.VISIBLE
            holder.cartBadgeTv.text = count.toString()
        } else {
            holder.cartBadgeTv.visibility = View.GONE
        }

        // show/hide map icon
        holder.mapIvProduct.visibility = if (showMap.contains(product.id)) View.VISIBLE else View.GONE

        // when click on cart icon -> count++ + show badge + show map + call addToCart
        holder.addToCartIv.setOnClickListener {
            val newCount = (counts[product.id] ?: 0) + 1
            counts[product.id] = newCount
            holder.cartBadgeTv.visibility = View.VISIBLE
            holder.cartBadgeTv.text = newCount.toString()

            // show map icon after first click
            showMap.add(product.id)
            holder.mapIvProduct.visibility = View.VISIBLE

            onAddToCart(product) // save in Firestore
        }

        // when click on map icon -> open map (route)
        holder.mapIvProduct.setOnClickListener { onMapClick(product) }
    }

    override fun getItemCount(): Int = productList.size
}
