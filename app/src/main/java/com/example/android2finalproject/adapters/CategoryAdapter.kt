    package com.example.android2finalproject.adapters


    import android.app.Activity
    import android.content.Intent
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.example.android2finalproject.R
    import com.esraa.shoppingapp.data.model.Category

    class CategoryAdapter(
    private val categoryList: List<Category>,
    private val onCategoryClick: (Category) -> Unit   //a call back ->in shoping fragment will cal it to open category products
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        private var selectedId: String? = null

        class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.cat_name)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }


        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = categoryList[position]
            holder.tvName.text = category.name

            holder.itemView.setOnClickListener {
                onCategoryClick(category)   //tell the fragment about this selected category
            }
        }

            override fun getItemCount(): Int = categoryList.size

        }

