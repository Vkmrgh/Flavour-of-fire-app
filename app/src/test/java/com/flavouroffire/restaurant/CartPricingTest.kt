package com.flavouroffire.restaurant

import com.flavouroffire.restaurant.model.CartLine
import com.flavouroffire.restaurant.model.Dish
import com.flavouroffire.restaurant.model.RestaurantUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class CartPricingTest {
    private val dish = Dish(1, "Test Butter Chicken", "Test", "Main Course", priceFull = 690.0, priceHalf = 410.0, image = 0)

    @Test fun fullSizeExtrasAndQuantityAreIncluded() {
        val line = CartLine(dish, quantity = 2, size = "Full", extras = setOf("Extra Butter", "Extra Cheese"))
        // (690 + 20 + 20) * 2 = 1460
        assertEquals(1460.0, line.total, 0.001)
    }

    @Test fun halfSizeUsesHalfPrice() {
        val line = CartLine(dish, quantity = 1, size = "Half")
        assertEquals(410.0, line.total, 0.001)
    }

    @Test fun promoDiscountAndDeliveryAreIncluded() {
        // subtotal 690, below the 599 free-delivery threshold does not apply since 690 > 599, so delivery is free
        val state = RestaurantUiState(cart = listOf(CartLine(dish)), promoApplied = true)
        // discount = 69, delivery = 0 (subtotal >= 599)
        assertEquals(621.0, state.total, 0.001)
    }
}
