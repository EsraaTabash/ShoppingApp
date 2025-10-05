package com.example.android2finalproject.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android2finalproject.R
import com.example.android2finalproject.adapters.CategoryAdapter
import com.example.android2finalproject.adapters.ProductAdapter
import com.example.android2finalproject.data.FirestoreService
import com.example.android2finalproject.models.Category
import com.example.android2finalproject.models.Product

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: RecyclerView.Adapter<*>

    private val categoryList = arrayListOf<Category>()
    private val productList  = arrayListOf<Product>()

    private enum class ScreenState { CATEGORIES, PRODUCTS }
    private var screenState = ScreenState.CATEGORIES

    private val fs = FirestoreService()

    // we need this flag when add prod in cart
    private var selectedCategoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        categoryAdapter = CategoryAdapter(categoryList) { cat ->
            // when cat click store its id
            selectedCategoryId = cat.id

            productList.clear()
            productAdapter.notifyDataSetChanged()
            showProducts()
            //when click on cat open its products
            loadProductsForCategory(cat.id)
        }
        //link cat with rv
        rvCategories.adapter = categoryAdapter

        rvProducts.layoutManager = LinearLayoutManager(requireContext())

        //when click on product + click on cart icon + click on map icon
        productAdapter = ProductAdapter(
            productList,
            onProductClick = { selectedProduct ->
                Toast.makeText(requireContext(), "Selected: ${selectedProduct.pName}", Toast.LENGTH_SHORT).show()
            },
            onAddToCart = { selectedProduct ->
                val catId = selectedCategoryId
                if (catId == null) {
                    Toast.makeText(requireContext(), "pick a category first", Toast.LENGTH_SHORT).show()
                } else {
                    fs.addToCart(
                        product = selectedProduct,
                        categoryId = catId,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Added to cart successfully", Toast.LENGTH_SHORT).show()
                            refreshCartBadge()
                        },
                        onError = { e ->
                            Toast.makeText(requireContext(), "fail add to cart=(: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            onMapClick = { p ->
                openMapForProduct(p)
            }
        )

        //link pro with its rv
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

    //update badge = sum of qty
    private fun refreshCartBadge() {
        fs.getCartCount(
            onSuccess = { total ->
                (requireActivity() as? CartBadgeHost)?.setCartBadge(total)
            },
            onError = {  }
        )
    }

    // ------------------------------------------------------------
    // open external Google Maps with a route (line) from current location -> product
    private fun openMapForProduct(p: Product) {
        val lat = p.latitude
        val lng = p.longitude

        if (lat != null && lng != null) {
            val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(navIntent)
            } catch (_: Exception) {
                val web = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving")
                startActivity(Intent(Intent.ACTION_VIEW, web))
            }
        } else {
            // no lat & lng ->search
            val search = Uri.parse("geo:0,0?q=" + Uri.encode(p.pName.ifBlank { "Product" }))
            val intent = Intent(Intent.ACTION_VIEW, search).apply {
                setPackage("com.google.android.apps.maps")
            }
            try { startActivity(intent) } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, search))
            }
        }
    }

}

// interface ->to update bdge
interface CartBadgeHost {
    fun setCartBadge(count: Long)
}
