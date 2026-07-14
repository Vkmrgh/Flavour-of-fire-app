package com.flavouroffire.restaurant.model

import androidx.annotation.DrawableRes

/**
 * priceFull is always set. priceHalf is only set for dishes that actually have a
 * Half/Full portion on the real menu (Tandoor, Chinese, Main Course Gravy, Rice).
 * Items with a single rate (Rolls, Combos, South Indian, Beverages) leave priceHalf null.
 */
data class Dish(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val priceFull: Double,
    val priceHalf: Double? = null,
    @DrawableRes val image: Int,
    val vegetarian: Boolean = false,
    val popular: Boolean = false,
)

data class CartLine(val dish: Dish, val quantity: Int = 1, val size: String = "Full", val extras: Set<String> = emptySet()) {
    val unitPrice: Double get() {
        val base = if (size == "Half" && dish.priceHalf != null) dish.priceHalf else dish.priceFull
        return base + extras.size * 20.0
    }
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
    val delivery: Double get() = if (cart.isEmpty()) 0.0 else if (subtotal >= 599.0) 0.0 else 40.0
    val total: Double get() = subtotal - discount + delivery
}
