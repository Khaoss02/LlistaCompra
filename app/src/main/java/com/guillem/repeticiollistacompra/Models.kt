package com.guillem.repeticiollistacompra

//Representa una llista (el "Workspace")
data class ShoppingList(
    val id: String = "",           // ID del document a Firestore
    val name: String = "",         // Nom de la llista (Ex: "Compra Casa")
    val owner: String = "",        // Email del creador
    val sharedWith: List<String> = emptyList() // Llista d'emails que hi tenen accés
)

// Representa una Categoria
data class Category(
    val id: String = "",
    val name: String = "",
    val color: String = "#000000" // Per diferenciar-les visualment
)

// Representa un Producte
data class Product(
    val id: String = "",
    val name: String = "",
    val completed: Boolean = false,val categoryId: String = "",
    val quantity: String = "1",
    val description: String = ""
)