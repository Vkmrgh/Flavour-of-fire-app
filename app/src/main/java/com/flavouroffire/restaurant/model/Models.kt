package com.flavouroffire.restaurant.model

import androidx.annotation.DrawableRes

data class Dish(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val minutes: Int,
    @DrawableRes val image: Int,
    val vegetarian: Boolean = false,
    val popular: Boolean = false,
)

data class CartLine(val dish: Dish, val quantity: Int = 1, val size: String = "Regular", val extras: Set<String> = emptySet()) {
    val unitPrice: Double get() = dish.price + (if (size == "Grande") 4.0 else 0.0) + extras.size * 2.5
    val total: Double get() = unitPrice * quantity
}

data class Order(val number: String, val lines: List<CartLine>, val total: Double, val eta: String)

data class RestaurantUiState(
    val query: String = "",
    val category: String = "All",
    val favorites: Set<Int> = emptySet(),
    val cart: List<CartLine> = emptyList(),
    val promoApplied: Boolean = false,
    val order: Order? = null,
) {
    val subtotal: Double get() = cart.sumOf { it.total }
    val discount: Double get() = if (promoApplied) subtotal * .10 else 0.0
    val delivery: Double get() = if (cart.isEmpty()) 0.0 else 3.5
    val total: Double get() = subtotal - discount + delivery
}
