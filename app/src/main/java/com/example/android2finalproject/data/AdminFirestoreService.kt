package com.example.android2finalproject.data

import com.example.android2finalproject.models.Category
import com.example.android2finalproject.models.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminFirestoreService {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    // category CRUD (everytime admin edit categories we will use 2 func)
    //create new category with auto id
    fun createCategory(
        name: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = db.collection("categories").document() //auto id
        val data = hashMapOf("name" to name)

        ref.set(data)
            .addOnSuccessListener {
                onSuccess(ref.id)
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    //update exist category name
    fun updateCategory(
        categoryId: String,
        newName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("categories")
            .document(categoryId)
            .update("name", newName)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    //delete category + all products inside it
    fun deleteCategory(
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val catRef = db.collection("categories").document(categoryId)

        //get products and delete them then delete category
        catRef.collection("products")
            .get()
            .addOnSuccessListener { querysnapshot ->
                val batch = db.batch()
                //delete every doc inside cat
                for (doc in querysnapshot.documents) batch.delete(doc.reference)
                batch.commit()
                    .addOnSuccessListener {
                        catRef.delete()
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onError(it)
                            }
                    }
                    .addOnFailureListener {
                        onError(it)
                    }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    // product CRUD
    //add product -> we send model and save its fields
    fun addProduct(
        categoryId: String,
        product: Product,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = db.collection("categories")
            .document(categoryId)
            .collection("products")
            .document()

        //convert model -> map
        val data = hashMapOf(
            "pName" to product.pName,
            "pDescription" to product.pDescription,
            "pPrice" to product.pPrice,
            "pType" to product.pType,
            "pRating" to product.pRating,
            "imageUri" to product.imageUri,
            "latitude"  to (product.latitude ?: 0.0),
            "longitude" to (product.longitude ?: 0.0)
        )

        ref.set(data)
            .addOnSuccessListener {
                onSuccess(ref.id)
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    //update exist product fields
    fun updateProduct(
        categoryId: String,
        productId: String,
        product: Product,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val map = mapOf(
            "pName" to product.pName,
            "pDescription" to product.pDescription,
            "pPrice" to product.pPrice,
            "pType" to product.pType,
            "pRating" to product.pRating,
            "imageUri" to product.imageUri,
            "latitude"  to (product.latitude ?: 0.0),
            "longitude" to (product.longitude ?: 0.0)
        )

        db.collection("categories")
            .document(categoryId)
            .collection("products")
            .document(productId)
            .update(map)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    //delete product
    fun deleteProduct(
        categoryId: String,
        productId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("categories")
            .document(categoryId)
            .collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    // statistics
    //get most rated product
    fun getTopRatedProduct(
        onSuccess: (Product?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        //require Firestore index for collectionGroup orderBy(pRating)
        db.collectionGroup("products")
            .orderBy("pRating", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapShot ->
                //get first element in the list or null if ther is nothing
                val doc = querySnapShot.documents.firstOrNull()
                if (doc == null)
                    onSuccess(null)
                else
                    onSuccess(mapProduct(doc))
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    //get most ordered product ====== sum qty in all users carts
    fun getMostOrderedProduct(
        onSuccess: (Pair<Product, Long>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collectionGroup("cart")
            .get()
            .addOnSuccessListener { qs ->
                val counts = hashMapOf<String,Long>()
                val meta   = hashMapOf<String,Pair<String,String>>()

                // unique key for prod -> "categoryId|$productId"

                for (doc in qs.documents) {
                    val pid = doc.getString("productId") ?: doc.id
                    val cid = doc.getString("categoryId") ?: ""
                    val qty = doc.getLong("qty") ?: 0L
                    val key = "$cid|$pid"
                    // put all qty in counts map
                    counts[key] = (counts[key] ?: 0L) + qty
                    //save prod key in another map
                    meta[key] = cid to pid
                    //conuts ->save sum for each product ordered by all users
                    //meta ->return a ref for prod in collection
                }

                val top = counts.maxByOrNull {
                    it.value
                }
                if (top == null) {
                    onSuccess(null);
                    return@addOnSuccessListener
                }
                //if we got the high ordered prod ->extract catid and prodid
                //get the prod
                val (catId, prodId) = meta[top.key]!!
                db.collection("categories")
                    .document(catId)
                    .collection("products")
                    .document(prodId)
                    .get()
                    .addOnSuccessListener { pdoc ->
                        if (!pdoc.exists()) {
                            onSuccess(null)
                        }
                        //convert to prod model
                        else onSuccess(mapProduct(pdoc) to top.value)
                    }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    // convert document -> Product model
    private fun mapProduct(doc: DocumentSnapshot): Product {
        val lat = doc.getDouble("latitude")
        val lng = doc.getDouble("longitude")
        return Product(
            pName  = doc.getString("pName") ?: "",
            pDescription = doc.getString("pDescription") ?: "",
            pPrice = doc.getDouble("pPrice") ?: 0.0,
            pType  = doc.getString("pType") ?: "",
            pRating = doc.getDouble("pRating") ?: 0.0,
            imageUri = doc.getString("imageUri") ?: "",
            id = doc.id,
            latitude = lat,
            longitude = lng
        )
    }
}
