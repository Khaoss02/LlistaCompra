package com.guillem.repeticiollistacompra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guillem.repeticiollistacompra.databinding.ActivityMainBinding
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicialització de Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 2. Configurar la interfície
        setupRecyclerView()
        setupUI()

        // 3. Login Anònim
        auth.signInAnonymously()
            .addOnSuccessListener {
                listenForShoppingLists()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error login: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = ShoppingListAdapter(
            lists = emptyList(),
            onClick = { shoppingList ->
                val intent = Intent(this, ListDetailsActivity::class.java)
                intent.putExtra("LIST_ID", shoppingList.id)
                intent.putExtra("LIST_NAME", shoppingList.name)
                startActivity(intent)
            },
            onDeleteClick = { shoppingList ->
                showDeleteConfirmationDialog(shoppingList)
            },
            onEditClick = { shoppingList ->
                showRenameDialog(shoppingList)
            }
        )
        binding.rvShoppingLists.layoutManager = LinearLayoutManager(this)
        binding.rvShoppingLists.adapter = adapter

        // --- FUNCIONALITAT DRAG & DROP ---
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // Obtenim la llista actual de l'adapter, la girem i actualitzem
                val currentList = adapter.getLists().toMutableList()
                Collections.swap(currentList, fromPosition, toPosition)
                adapter.updateData(currentList)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            // Efecte visual en seleccionar (estètica)
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.8f
                    viewHolder?.itemView?.scaleX = 1.05f
                    viewHolder?.itemView?.scaleY = 1.05f
                }
            }

            // Restaurar estètica al deixar anar
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                viewHolder.itemView.scaleX = 1.0f
                viewHolder.itemView.scaleY = 1.0f
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvShoppingLists)
    }

    private fun listenForShoppingLists() {
        val userId = auth.currentUser?.uid
        val query = if (userId != null) {
            db.collection("shoppingLists").whereArrayContains("sharedWith", userId)
        } else {
            db.collection("shoppingLists")
        }

        query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val listObjects = snapshot?.documents?.mapNotNull { doc ->
                val shoppingList = doc.toObject(ShoppingList::class.java)
                shoppingList?.copy(id = doc.id)
            } ?: emptyList()
            adapter.updateData(listObjects)
        }
    }

    private fun setupUI() {
        binding.fabAddList.setOnClickListener { showCreateListDialog() }
        binding.btnJoinList.setOnClickListener { joinListDialog() }
    }

    private fun showCreateListDialog() {
        val input = EditText(this)
        input.hint = "Ex: Compra Setmanal"
        AlertDialog.Builder(this)
            .setTitle("Nova Llista")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) createNewList(name)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun createNewList(name: String) {
        val userId = auth.currentUser?.uid ?: "usuari_desconegut"
        val newList = ShoppingList(name = name, owner = userId, sharedWith = listOf(userId))
        db.collection("shoppingLists").add(newList)
    }

    private fun showRenameDialog(shoppingList: ShoppingList) {
        val input = EditText(this)
        input.setText(shoppingList.name)
        AlertDialog.Builder(this)
            .setTitle("Canviar nom")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) updateListName(shoppingList.id, newName)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun updateListName(listId: String, newName: String) {
        db.collection("shoppingLists").document(listId).update("name", newName)
    }

    private fun showDeleteConfirmationDialog(shoppingList: ShoppingList) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar llista")
            .setMessage("Vols eliminar '${shoppingList.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("shoppingLists").document(shoppingList.id).delete()
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun joinListDialog() {
        val input = EditText(this)
        input.hint = "Codi de la llista"
        AlertDialog.Builder(this)
            .setTitle("Unir-se")
            .setView(input)
            .setPositiveButton("Afegir") { _, _ ->
                val code = input.text.toString().trim()
                if (code.isNotEmpty()) joinList(code)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun joinList(listCode: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("shoppingLists").document(listCode)
            .update("sharedWith", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
            .addOnFailureListener {
                Toast.makeText(this, "Codi no vàlid", Toast.LENGTH_SHORT).show()
            }
    }
}