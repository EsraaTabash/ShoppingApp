package com.example.android2finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esraa.shoppingapp.data.model.Category
import com.esraa.shoppingapp.data.model.Product
import com.example.android2finalproject.R
import com.example.android2finalproject.adapters.CategoryAdapter
import com.example.android2finalproject.adapters.ProductAdapter
import com.example.android2finalproject.data.FirestoreService

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingFragment : Fragment() {
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null


    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: RecyclerView.Adapter<*>

    private val categoryList = arrayListOf<Category>()
    private val productList  = arrayListOf<Product>()

    private enum class ScreenState { CATEGORIES, PRODUCTS }
    private var screenState = ScreenState.CATEGORIES

    private val fs = FirestoreService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //temporary data
//        categoryList.clear()
//        categoryList.add(Category("1", "Electronics"))
//        categoryList.add(Category("2", "Clothes"))
//        categoryList.add(Category("3", "Books"))
//        categoryList.add(Category("4", "Shoes"))
//        categoryList.add(Category("5", "Accessories"))

        //handle back process from products to categories
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (screenState == ScreenState.PRODUCTS) {
                        showCategories()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCategories  = view.findViewById(R.id.rvCategories)
        rvProducts    = view.findViewById(R.id.rvProducts)

        rvCategories.layoutManager = LinearLayoutManager(requireContext())

        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(categoryList) { cat ->
            productList.clear()
            productAdapter.notifyDataSetChanged()
            showProducts()
            //when click on cat open its products
            loadProductsForCategory(cat.id)
        }
        //link cat with rv
        rvCategories.adapter = categoryAdapter
        rvProducts.layoutManager = LinearLayoutManager(requireContext())

        //when click on product
        productAdapter = ProductAdapter(productList) { selectedProduct ->
            Toast.makeText(requireContext(), "Selected: ${selectedProduct.pName}", Toast.LENGTH_SHORT).show()
        }

        //link pro with its rv
        rvProducts.adapter = productAdapter
        rvProducts.adapter = productAdapter

        //load data from firebase instead of temp data
        showCategories()
        loadCategoriesFromFirestore()
    }

    //switch between two states
    //s1
    private fun showCategories() {
        screenState = ScreenState.CATEGORIES
        rvProducts.visibility   = View.GONE
        rvCategories.visibility = View.VISIBLE
    }
    //s2
    private fun showProducts() {
        screenState = ScreenState.PRODUCTS
        rvCategories.visibility = View.GONE
        rvProducts.visibility   = View.VISIBLE
    }

    private fun loadCategoriesFromFirestore() {
        fs.getCategories(
            onSuccess = { list ->
                categoryList.clear()
                categoryList.addAll(list)
                categoryAdapter.notifyDataSetChanged()
                showCategories()
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load categories: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadProductsForCategory(categoryId: String) {
//        //temp data
//        productList.clear()
//        when (categoryId) {
//            "1" -> {
//                productList.add(Product("p1", "Phone", 299.0))
//                productList.add(Product("p2", "Headset", 49.0))
//            }
//            "2" -> {
//                productList.add(Product("p3", "T-Shirt", 19.0))
//                productList.add(Product("p4", "Jacket", 79.0))
//            }
//            else -> {
//                productList.add(Product("p5", "Generic Item", 9.99))
//            }
//        }
//        //notify any registered observers that the data set has changed.
//        productAdapter.notifyDataSetChanged()

        //load category products from firebase
        fs.getProductsOfCategory(
            categoryId = categoryId,
            onSuccess = { list ->
                productList.clear()
                productList.addAll(list)
                productAdapter.notifyDataSetChanged()
                showProducts()
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load products: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
