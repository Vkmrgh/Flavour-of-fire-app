package com.flavouroffire.restaurant.data

import com.flavouroffire.restaurant.R
import com.flavouroffire.restaurant.model.Dish

// TODO: swap category names / items below for your actual menu.
// Set vegetarian = true for veg dishes, false for non-veg, matching what the phone
// reservation agent already asks callers about.
val categories = listOf("All", "Starters", "Grill & Tandoor", "Curries", "Biryani", "Dessert")

val menu = listOf(
    Dish(1, "Paneer Tikka", "Char-grilled cottage cheese, bell peppers and onion, smoked over open flame.", "Starters", 12.0, 4.8, 15, R.drawable.dish_pasta, vegetarian = true, popular = true),
    Dish(2, "Flame-Grilled Chicken Tikka", "Yogurt-marinated chicken, char-grilled and finished with smoked butter.", "Grill & Tandoor", 15.0, 4.9, 20, R.drawable.dish_pizza, popular = true),
    Dish(3, "Tandoori Lamb Chops", "Slow-marinated lamb chops, fire-roasted to order.", "Grill & Tandoor", 22.0, 4.9, 25, R.drawable.dish_pizza, popular = true),
    Dish(4, "Dal Makhani", "Slow-simmered black lentils finished with cream and smoked butter.", "Curries", 13.0, 4.7, 20, R.drawable.hero_italian, vegetarian = true),
    Dish(5, "Butter Chicken", "Tandoor-roasted chicken in a rich tomato-cashew gravy.", "Curries", 16.0, 4.9, 22, R.drawable.dish_pasta, popular = true),
    Dish(6, "Vegetable Dum Biryani", "Basmati rice layered with spiced vegetables, slow-cooked in a sealed pot.", "Biryani", 14.0, 4.6, 25, R.drawable.dish_pasta, vegetarian = true),
    Dish(7, "Fire Chicken Biryani", "Basmati rice, marinated chicken and whole spices, dum-cooked.", "Biryani", 17.0, 4.8, 25, R.drawable.dish_pizza, popular = true),
    Dish(8, "Gulab Jamun", "Warm milk-solid dumplings soaked in cardamom-rose syrup.", "Dessert", 6.0, 4.8, 8, R.drawable.dish_tiramisu, vegetarian = true, popular = true),
)
