package com.guillem.repeticiollistacompra

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.guillem.repeticiollistacompra.databinding.ActivityListDetailsBinding

class ListDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListDetailsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductAdapter
    private var listId: String? = null

    // Llista dinàmica que s'omplirà des de Firebase
    private var categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        listId = intent.getStringExtra("LIST_ID")
        val listName = intent.getStringExtra("LIST_NAME")
        binding.tvTitle.text = listName

        setupRecyclerView()

        // Primer escoltem les categories, i un cop les tinguem, escoltem els productes
        listenForCategories()

        binding.btnAddProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val spinnerPos = binding.spinnerCategory.selectedItemPosition

            if (spinnerPos < categories.size && spinnerPos >= 0) {
                val selectedCategory = categories[spinnerPos]
                if (name.isNotEmpty()) addProduct(name, selectedCategory.id)
            } else {
                Toast.makeText(this, "Selecciona una categoria vàlida", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener { showShareDialog(listId) }
        binding.btnToggleVisibility.setOnClickListener { adapter.toggleVisibility() }
    }

    private fun listenForCategories() {
        val id = listId ?: return

        db.collection("shoppingLists").document(id).collection("categories")
            .addSnapshotListener { snapshot, _ ->
                val firebaseCategories = snapshot?.documents?.mapNotNull { doc ->
                    val cat = doc.toObject(Category::class.java)
                    cat?.copy(id = doc.id)
                }?.sortedBy { it.name } ?: emptyList() // ORDRE ALFABÈTIC

                if (firebaseCategories.isEmpty()) {
                    seedDefaultCategories(id)
                } else {
                    categories.clear()
                    categories.addAll(firebaseCategories)
                    setupSpinner()
                    listenForProducts() // Refresquem productes per si ha canviat l'ordre de les categories
                }
            }
    }

    private fun seedDefaultCategories(id: String) {
        val defaultCats = listOf(
            Category("", "Alimentació", "#FF9800"),
            Category("", "Begudes", "#2196F3"),
            Category("", "Neteja", "#4CAF50"),
            Category("", "Altres", "#000000")
        )
        val batch = db.batch()
        defaultCats.forEach { cat ->
            val ref = db.collection("shoppingLists").document(id).collection("categories").document()
            batch.set(ref, cat)
        }
        batch.commit()
    }

    private fun setupSpinner() {
        // Els noms ja estaran ordenats perquè 'categories' ja ho està
        val displayNames = categories.map { it.name }.toMutableList()
        displayNames.add("+ Nova categoria...")

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == displayNames.size - 1) {
                    showCreateCategoryDialog()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showCreateCategoryDialog() {
        val input = EditText(this)
        input.hint = "Nom de la categoria"

        AlertDialog.Builder(this)
            .setTitle("Crear Nova Categoria")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Crear") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    saveCategoryToFirebase(newName)
                } else {
                    binding.spinnerCategory.setSelection(0)
                }
            }
            .setNegativeButton("Cancel·lar") { _, _ -> binding.spinnerCategory.setSelection(0) }
            .show()
    }

    private fun saveCategoryToFirebase(name: String) {
        val id = listId ?: return
        val newCat = Category("", name, "#607D8B")

        db.collection("shoppingLists").document(id).collection("categories")
            .add(newCat)
            .addOnSuccessListener {
                Toast.makeText(this, "Categoria creada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            allProducts = emptyList(),
            categories = categories,
            onCheckedChange = { product, isChecked -> updateProductStatus(product, isChecked) },
            onDelete = { product -> deleteProduct(product) },
            onEdit = { product -> showEditDialog(product) }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun listenForProducts() {
        val id = listId ?: return

        db.collection("shoppingLists").document(id).collection("products")
            .addSnapshotListener { snapshot, _ ->
                // La prioritat ara dependrà de l'ordre alfabètic de les categories
                val categoryPriority = categories.withIndex().associate { it.value.id to it.index }

                val productList = snapshot?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Product::class.java)
                    p?.copy(id = doc.id)
                } ?: emptyList()

                val sortedList = productList.sortedWith(
                    compareBy<Product> { it.completed }
                        .thenBy { categoryPriority[it.categoryId] ?: 99 }
                        .thenBy { it.name }
                )

                adapter.updateData(sortedList, categories)
            }
    }

    private fun showEditDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dades del producte")
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 20, 60, 0)
        }

        val inputName = EditText(this).apply { setText(product.name) }
        layout.addView(createLabel("Nom:"))
        layout.addView(inputName)

        val inputQuantity = EditText(this).apply { setText(product.quantity) }
        layout.addView(createLabel("Quantitat:"))
        layout.addView(inputQuantity)

        val spinnerCat = android.widget.Spinner(this)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.map { it.name })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCat.adapter = spinnerAdapter

        val currentCatIndex = categories.indexOfFirst { it.id == product.categoryId }
        if (currentCatIndex != -1) spinnerCat.setSelection(currentCatIndex)

        layout.addView(createLabel("Categoria:"))
        layout.addView(spinnerCat)

        builder.setView(layout)
        builder.setPositiveButton("Guardar") { _, _ ->
            val catId = categories[spinnerCat.selectedItemPosition].id
            saveProductDetails(product.id, inputName.text.toString(), inputQuantity.text.toString(), "", catId)
        }
        builder.setNegativeButton("Cancel·lar", null)
        builder.show()
    }

    private fun createLabel(text: String) = android.widget.TextView(this).apply {
        this.text = text
        setPadding(0, 20, 0, 5)
    }

    private fun addProduct(name: String, catId: String) {
        val id = listId ?: return
        val newProduct = Product(name = name, completed = false, categoryId = catId)
        db.collection("shoppingLists").document(id).collection("products").add(newProduct)
            .addOnSuccessListener { binding.etProductName.text.clear() }
    }

    private fun saveProductDetails(productId: String, name: String, qty: String, desc: String, catId: String) {
        val id = listId ?: return
        val updates = mapOf("name" to name, "quantity" to qty, "description" to desc, "categoryId" to catId)
        db.collection("shoppingLists").document(id).collection("products").document(productId).update(updates)
    }

    private fun updateProductStatus(product: Product, isChecked: Boolean) {
        val id = listId ?: return
        db.collection("shoppingLists").document(id).collection("products").document(product.id).update("completed", isChecked)
    }

    private fun deleteProduct(product: Product) {
        val id = listId ?: return
        db.collection("shoppingLists").document(id).collection("products").document(product.id).delete()
    }

    private fun showShareDialog(fullId: String?) {
        val uid = fullId ?: "Error"
        AlertDialog.Builder(this)
            .setTitle("Compartir")
            .setMessage("Codi: $uid")
            .setPositiveButton("Copiar") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ListID", uid))
            }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}