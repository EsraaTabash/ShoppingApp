package com.example.android2finalproject.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android2finalproject.R
import com.example.android2finalproject.adapters.AdminCategoryAdapter
import com.example.android2finalproject.adapters.AdminProductAdapter
import com.example.android2finalproject.data.AdminFirestoreService
import com.example.android2finalproject.data.FirestoreService
import com.example.android2finalproject.models.Category
import com.example.android2finalproject.models.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var catAdapter: RecyclerView.Adapter<*>
    private lateinit var prodAdapter: RecyclerView.Adapter<*>
    private val categoryList = arrayListOf<Category>()
    private val productList  = arrayListOf<Product>()
    private enum class ScreenState { CATEGORIES, PRODUCTS }
    private var screenState = ScreenState.CATEGORIES
    private val fs = FirestoreService()
    private val adminFs = AdminFirestoreService()
    private var selectedCategory: Category? = null
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCategories = view.findViewById(R.id.rvCategoriesAdmin)
        rvProducts  = view.findViewById(R.id.rvProductsAdmin)
        fab = view.findViewById(R.id.fabAdd)
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        catAdapter = AdminCategoryAdapter(
            categoryList  = categoryList,
            onOpenCategory = { cat ->
                selectedCategory = cat
                loadProducts(cat.id)
            },
            onEditCategory = { cat ->
                showRenameCategoryDialog(cat)
            },
            onDeleteCategory = { cat ->
                adminFs.deleteCategory(
                    categoryId = cat.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "fail delete: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
        rvCategories.adapter = catAdapter
        prodAdapter = AdminProductAdapter(
            list = productList,
            onEdit = { p -> showEditProductDialog(p) },
            onDelete = { p ->
                val cat = selectedCategory ?: return@AdminProductAdapter
                adminFs.deleteProduct(
                    categoryId = cat.id,
                    productId  = p.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                        loadProducts(cat.id)
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "fail delete: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
        rvProducts.adapter = prodAdapter

        //fab-> add category or product
        fab.setOnClickListener { showAddDialog() }

        //load initial data
        showCategories()
        loadCategories()
    }

    //switch between screens
    private fun showCategories() {
        screenState = ScreenState.CATEGORIES
        rvCategories.visibility = View.VISIBLE
        rvProducts.visibility   = View.GONE
    }
    private fun showProducts() {
        screenState = ScreenState.PRODUCTS
        rvCategories.visibility = View.GONE
        rvProducts.visibility   = View.VISIBLE
    }

    //load categories
    private fun loadCategories() {
        fs.getCategories(
            onSuccess = { list ->
                categoryList.clear()
                categoryList.addAll(list)
                catAdapter.notifyDataSetChanged()
                showCategories()
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load categories): ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    //load products for one category
    private fun loadProducts(catId: String) {
        fs.getProductsOfCategory(
            categoryId = catId,
            onSuccess = { list ->
                productList.clear()
                productList.addAll(list)
                prodAdapter.notifyDataSetChanged()
                showProducts()
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load products: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showAddDialog() {
        val items = arrayOf("Add Category", "Add Product")
        AlertDialog.Builder(requireContext())
            .setTitle("Dashboard actions")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showCreateCategoryDialog()
                    1 -> showQuickAddProductDialog()
                }
            }.show()
    }

    // create category
    private fun showCreateCategoryDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Category name"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(requireContext())
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Save") { d, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "name required", Toast.LENGTH_SHORT).show()
                } else {
                    adminFs.createCategory(
                        name = name,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Category added!", Toast.LENGTH_SHORT).show()
                            loadCategories()
                        },
                        onError = { e ->
                            Toast.makeText(requireContext(), "fail add): ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameCategoryDialog(cat: Category) {
        val input = EditText(requireContext()).apply {
            setText(cat.name)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Rename Category")
            .setView(input)
            .setPositiveButton("Save") { d, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "name required", Toast.LENGTH_SHORT).show()
                } else {
                    adminFs.updateCategory(
                        categoryId = cat.id,
                        newName = newName,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                            loadCategories()
                        },
                        onError = { e ->
                            Toast.makeText(requireContext(), "fail update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showQuickAddProductDialog() {
        val cat = selectedCategory
        if (cat == null) {
            Toast.makeText(requireContext(), "pick a category first", Toast.LENGTH_SHORT).show()
            return
        }

        val container = layoutInflater.inflate(R.layout.dialog_quick_add_product, null)

        val etName  = container.findViewById<EditText>(R.id.etName)
        val etPrice  = container.findViewById<EditText>(R.id.etPrice)
        val etType  = container.findViewById<EditText>(R.id.etType)
        val etDescription = container.findViewById<EditText>(R.id.etDescription)
        val etRating = container.findViewById<EditText>(R.id.etRating)
        val etImageUri  = container.findViewById<EditText>(R.id.etImageUri)
        val etLatitude = container.findViewById<EditText>(R.id.etLatitude)
        val etLongitude = container.findViewById<EditText>(R.id.etLongitude)

        // build dialog for add
        AlertDialog.Builder(requireContext())
            .setTitle("New Product")
            .setView(container)
            .setPositiveButton("Save") { d, _ ->
                // read values from user inputs
                val name  = etName.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val type  = etType.text.toString().trim()
                val desc  = etDescription.text.toString().trim()
                val rate  = (etRating.text.toString().toDoubleOrNull() ?: 0.0).coerceIn(0.0, 5.0)
                val img   = etImageUri.text.toString().trim()
                val lat   = etLatitude.text.toString().toDoubleOrNull()
                val lng   = etLongitude.text.toString().toDoubleOrNull()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "name required", Toast.LENGTH_SHORT).show()
                } else {
                    // create product model
                    val p = Product(
                        pName = name,
                        pDescription = desc,
                        pPrice = price,
                        pType = type,
                        pRating = rate,
                        imageUri = img,
                        id = "",
                        latitude = lat,
                        longitude = lng
                    )
                    // save in Firestore
                    adminFs.addProduct(
                        categoryId = cat.id,
                        product = p,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Product added", Toast.LENGTH_SHORT).show()
                            loadProducts(cat.id) // refresh list
                        },
                        onError = { e ->
                            Toast.makeText(requireContext(), "fail add: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // edit product
    private fun showEditProductDialog(p: Product) {
        val cat = selectedCategory ?: return
        val container = layoutInflater.inflate(R.layout.dialog_quick_add_product, null)

        val etName = container.findViewById<EditText>(R.id.etName)
        val etPrice = container.findViewById<EditText>(R.id.etPrice)
        val etType  = container.findViewById<EditText>(R.id.etType)
        val etDescription = container.findViewById<EditText>(R.id.etDescription)
        val etRating = container.findViewById<EditText>(R.id.etRating)
        val etImageUri = container.findViewById<EditText>(R.id.etImageUri)
        val etLatitude  = container.findViewById<EditText>(R.id.etLatitude)
        val etLongitude  = container.findViewById<EditText>(R.id.etLongitude)

        etName.setText(p.pName)
        etPrice.setText(p.pPrice.toString())
        etType.setText(p.pType)
        etDescription.setText(p.pDescription)
        etRating.setText(p.pRating.toString())
        etImageUri.setText(p.imageUri)
        etLatitude.setText(p.latitude?.toString() ?: "")
        etLongitude.setText(p.longitude?.toString() ?: "")

        //show dialog and save
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Product")
            .setView(container)
            .setPositiveButton("Save") { d, _ ->
                val name  = etName.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val type  = etType.text.toString().trim()
                val desc  = etDescription.text.toString().trim()
                val rate  = (etRating.text.toString().toDoubleOrNull() ?: 0.0).coerceIn(0.0, 5.0)
                val img   = etImageUri.text.toString().trim()
                val lat   = etLatitude.text.toString().toDoubleOrNull()
                val lng   = etLongitude.text.toString().toDoubleOrNull()
                val updated = p.copy(
                    pName = name,
                    pDescription = desc,
                    pPrice = price,
                    pType = type,
                    pRating = rate,
                    imageUri = img,
                    latitude = lat,
                    longitude = lng
                )
                adminFs.updateProduct(
                    categoryId = cat.id,
                    productId  = p.id,
                    product    = updated,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                        loadProducts(cat.id)
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "fail update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
