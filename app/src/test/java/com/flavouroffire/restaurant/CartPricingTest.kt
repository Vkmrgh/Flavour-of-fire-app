package com.flavouroffire.restaurant

import com.flavouroffire.restaurant.model.CartLine
import com.flavouroffire.restaurant.model.Dish
import com.flavouroffire.restaurant.model.RestaurantUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class CartPricingTest {
    private val dish = Dish(1, "Test pasta", "Test", "Pasta", 20.0, 4.9, 20, 0)

    @Test fun grandeExtrasAndQuantityAreIncluded() {
        val line = CartLine(dish, quantity = 2, size = "Grande", extras = setOf("Truffle", "Burrata"))
        assertEquals(58.0, line.total, 0.001)
    }

    @Test fun promoDiscountAndDeliveryAreIncluded() {
        val state = RestaurantUiState(cart = listOf(CartLine(dish)), promoApplied = true)
        assertEquals(21.5, state.total, 0.001)
    }
}
