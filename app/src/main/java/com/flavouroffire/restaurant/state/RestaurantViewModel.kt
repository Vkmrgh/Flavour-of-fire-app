package com.flavouroffire.restaurant.state

import androidx.lifecycle.ViewModel
import com.flavouroffire.restaurant.data.menu
import com.flavouroffire.restaurant.model.CartLine
import com.flavouroffire.restaurant.model.Dish
import com.flavouroffire.restaurant.model.Order
import com.flavouroffire.restaurant.model.RestaurantUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RestaurantViewModel : ViewModel() {
    private val _state = MutableStateFlow(RestaurantUiState())
    val state = _state.asStateFlow()

    fun setQuery(value: String) = _state.update { it.copy(query = value) }
    fun setCategory(value: String) = _state.update { it.copy(category = value) }
    fun toggleFavorite(id: Int) = _state.update {
        it.copy(favorites = if (id in it.favorites) it.favorites - id else it.favorites + id)
    }
    fun addToCart(dish: Dish, size: String, extras: Set<String>, quantity: Int) = _state.update { state ->
        state.copy(cart = state.cart + CartLine(dish, quantity, size, extras))
    }
    fun changeQuantity(index: Int, delta: Int) = _state.update { state ->
        val current = state.cart[index]
        val updated = current.copy(quantity = current.quantity + delta)
        state.copy(cart = if (updated.quantity <= 0) state.cart.filterIndexed { i, _ -> i != index } else state.cart.mapIndexed { i, line -> if (i == index) updated else line })
    }
    fun applyPromo() = _state.update { it.copy(promoApplied = true) }
    fun placeOrder() = _state.update { state ->
        state.copy(order = Order("TV-2841", state.cart, state.total, "25–35 min"), cart = emptyList(), promoApplied = false)
    }
    fun filteredDishes(state: RestaurantUiState): List<Dish> = menu.filter {
        (state.category == "All" || it.category == state.category) &&
            (state.query.isBlank() || it.name.contains(state.query, true) || it.description.contains(state.query, true))
    }
}
