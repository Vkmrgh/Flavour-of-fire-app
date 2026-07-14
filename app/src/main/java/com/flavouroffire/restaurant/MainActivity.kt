package com.flavouroffire.restaurant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flavouroffire.restaurant.data.categories
import com.flavouroffire.restaurant.data.menu
import com.flavouroffire.restaurant.model.Dish
import com.flavouroffire.restaurant.model.RestaurantUiState
import com.flavouroffire.restaurant.state.RestaurantViewModel
import com.flavouroffire.restaurant.ui.theme.FlavourOfFireTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FlavourOfFireTheme { FlavourOfFireApp() } }
    }
}

private fun money(value: Double) = "₹" + String.format(java.util.Locale.US, "%,.0f", value)
private val tabs = listOf("home", "search", "orders", "profile")

@Composable
fun FlavourOfFireApp(vm: RestaurantViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val route = backStackEntry?.destination?.route
            if (route in tabs) NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                listOf(
                    Triple("home", "Home", Icons.Outlined.Home), Triple("search", "Search", Icons.Outlined.Search),
                    Triple("orders", "Orders", Icons.Outlined.ReceiptLong), Triple("profile", "Profile", Icons.Outlined.Person)
                ).forEach { (path, label, icon) ->
                    NavigationBarItem(selected = route == path, onClick = { nav.navigate(path) { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true } }, icon = { Icon(icon, null) }, label = { Text(label) })
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(state, vm, nav) }
            composable("search") { BrowseScreen(state, vm, nav) }
            composable("orders") { OrdersScreen(state, nav) }
            composable("profile") { ProfileScreen() }
            composable("dish/{id}") { back -> DishScreen(menu.first { it.id == back.arguments?.getString("id")?.toInt() }, state, vm, nav) }
            composable("cart") { CartScreen(state, vm, nav) }
            composable("checkout") { CheckoutScreen(state, vm, nav) }
            composable("success") { SuccessScreen(state, nav) }
        }
    }
}

@Composable
private fun HomeScreen(state: RestaurantUiState, vm: RestaurantViewModel, nav: NavController) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { TopHeader(state.cart.sumOf { it.quantity }, nav) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Namaste! 🔥", style = MaterialTheme.typography.displaySmall)
                Text("What shall we fire up for you today?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(28.dp))) {
                Image(painterResource(R.drawable.dish_pizza), "Flame-grilled tandoori dishes", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onBackground.copy(alpha = .45f)))
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Flavour of Fire · We Made It Easy", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.background)
                    Text("Free delivery within 3 km on orders above ₹599", color = MaterialTheme.colorScheme.background)
                }
            }
        }
        item { CategoryRow(state.category, vm::setCategory) }
        item { SectionTitle("Most loved", "See all") { nav.navigate("search") } }
        items(menu.filter { it.popular && (state.category == "All" || it.category == state.category) }) { dish -> DishCard(dish, dish.id in state.favorites, { vm.toggleFavorite(dish.id) }) { nav.navigate("dish/${dish.id}") } }
    }
}

@Composable
private fun TopHeader(count: Int, nav: NavController) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) { Text("FLAVOUR OF FIRE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text("D4, Sector 85, Faridabad", fontWeight = FontWeight.SemiBold) }
    BadgedBox(badge = { if (count > 0) Badge { Text(count.toString()) } }) { IconButton(onClick = { nav.navigate("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Open cart") } }
}

@Composable
private fun BrowseScreen(state: RestaurantUiState, vm: RestaurantViewModel, nav: NavController) {
    val results = vm.filteredDishes(state)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Find your favorite", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f)); IconButton({ nav.navigate("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Cart") } } }
        item { OutlinedTextField(state.query, vm::setQuery, Modifier.fillMaxWidth(), placeholder = { Text("Search tikka, biryani, dosa…") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { CategoryRow(state.category, vm::setCategory) }
        item { Text("${results.size} dishes", style = MaterialTheme.typography.titleMedium) }
        items(results) { dish -> DishCard(dish, dish.id in state.favorites, { vm.toggleFavorite(dish.id) }) { nav.navigate("dish/${dish.id}") } }
        if (results.isEmpty()) item { EmptyState("Nothing on the menu matches that yet.", "Clear search") { vm.setQuery(""); vm.setCategory("All") } }
    }
}

@Composable
private fun CategoryRow(selected: String, select: (String) -> Unit) = LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(categories) { category -> FilterChip(selected == category, { select(category) }, { Text(category) }) }
}

@Composable
private fun DishCard(dish: Dish, favorite: Boolean, toggle: () -> Unit, open: () -> Unit) {
    Card(onClick = open, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(painterResource(dish.image), dish.name, Modifier.size(112.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f).heightIn(min = 112.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row { Text(dish.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis); IconButton(toggle, Modifier.size(40.dp)) { Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Favorite", tint = MaterialTheme.colorScheme.primary) } }
                Text(dish.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (dish.vegetarian) "\uD83D\uDFE2 Veg" else "\uD83D\uDD34 Non-Veg", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    if (dish.priceHalf != null) Text("Half ${money(dish.priceHalf)} · Full ${money(dish.priceFull)}", fontWeight = FontWeight.Bold)
                    else Text(money(dish.priceFull), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DishScreen(dish: Dish, state: RestaurantUiState, vm: RestaurantViewModel, nav: NavController) {
    var size by remember { mutableStateOf("Full") }; var extras by remember { mutableStateOf(setOf<String>()) }; var quantity by remember { mutableIntStateOf(1) }
    val unit = (if (size == "Half" && dish.priceHalf != null) dish.priceHalf else dish.priceFull) + extras.size * 20.0
    Scaffold(topBar = { TopAppBar(title = {}, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }, actions = { IconButton({ vm.toggleFavorite(dish.id) }) { Icon(if (dish.id in state.favorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Favorite") } }) }, bottomBar = { Button({ vm.addToCart(dish, size, extras, quantity); nav.navigate("cart") }, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("Add to bag  ·  ${money(unit * quantity)}") } }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { Image(painterResource(dish.image), dish.name, Modifier.fillMaxWidth().height(280.dp), contentScale = ContentScale.Crop) }
            item { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(dish.name, style = MaterialTheme.typography.displaySmall); Text("${if (dish.vegetarian) "\uD83D\uDFE2 Vegetarian" else "\uD83D\uDD34 Non-Vegetarian"}  ·  ${dish.category}", color = MaterialTheme.colorScheme.secondary); Text(dish.description, style = MaterialTheme.typography.bodyLarge)
                if (dish.priceHalf != null) { Text("Choose a portion", style = MaterialTheme.typography.titleLarge); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Half" to dish.priceHalf, "Full" to dish.priceFull).forEach { (label, p) -> FilterChip(size == label, { size = label }, { Text("$label · ${money(p)}") }) } } }
                Text("Add something extra", style = MaterialTheme.typography.titleLarge); listOf("Extra Butter", "Extra Cheese", "Extra Spicy").forEach { extra -> Row(Modifier.fillMaxWidth().clickable { extras = if (extra in extras) extras - extra else extras + extra }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(extra in extras, { checked -> extras = if (checked) extras + extra else extras - extra }); Text(extra, Modifier.weight(1f)); Text("+${money(20.0)}") } }
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Quantity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)); QuantityControl(quantity, { if (quantity > 1) quantity-- }, { quantity++ }) }
            } }
        }
    }
}

@Composable
private fun CartScreen(state: RestaurantUiState, vm: RestaurantViewModel, nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Your bag") }, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }) }, bottomBar = { if (state.cart.isNotEmpty()) Button({ nav.navigate("checkout") }, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("Checkout  ·  ${money(state.total)}") } }) { pad ->
        if (state.cart.isEmpty()) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { EmptyState("Your bag is waiting for something delicious.", "Browse the menu") { nav.navigate("home") } }
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(state.cart.size) { index -> val line = state.cart[index]; Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { Image(painterResource(line.dish.image), line.dish.name, Modifier.size(82.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop); Column(Modifier.weight(1f)) { Text(line.dish.name, style = MaterialTheme.typography.titleMedium); Text("${line.size}${if (line.extras.isEmpty()) "" else " · ${line.extras.joinToString()}"}", style = MaterialTheme.typography.bodyMedium); Text(money(line.total), fontWeight = FontWeight.Bold) }; QuantityControl(line.quantity, { vm.changeQuantity(index, -1) }, { vm.changeQuantity(index, 1) }) } }
            item { HorizontalDivider(); Summary(state); OutlinedButton(vm::applyPromo, Modifier.fillMaxWidth(), enabled = !state.promoApplied) { Icon(Icons.Outlined.LocalOffer, null); Spacer(Modifier.width(8.dp)); Text(if (state.promoApplied) "FIRE10 applied" else "Apply FIRE10") } }
        }
    }
}

@Composable
private fun CheckoutScreen(state: RestaurantUiState, vm: RestaurantViewModel, nav: NavController) {
    var payment by remember { mutableStateOf("Visa •••• 4242") }
    Scaffold(topBar = { TopAppBar(title = { Text("Checkout") }, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }) }, bottomBar = { Button({ vm.placeOrder(); nav.navigate("success") { popUpTo("home") } }, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("Place order  ·  ${money(state.total)}") } }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item { CheckoutBlock("Delivery address", Icons.Outlined.LocationOn, "Your saved address", "Sector 85, Faridabad, Haryana") }
            item { CheckoutBlock("Delivery time", Icons.Outlined.Schedule, "As soon as possible", "25–35 minutes") }
            item { Text("Payment", style = MaterialTheme.typography.titleLarge); listOf("Visa •••• 4242", "Cash on delivery").forEach { option -> Row(Modifier.fillMaxWidth().clickable { payment = option }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(payment == option, { payment = option }); Text(option) } }; Text("Prototype only — no payment details are collected.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { HorizontalDivider(); Summary(state) }
        }
    }
}

@Composable
private fun SuccessScreen(state: RestaurantUiState, nav: NavController) {
    val order = state.order
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(28.dp)); Surface(shape = RoundedCornerShape(100.dp), color = MaterialTheme.colorScheme.secondary) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(20.dp).size(36.dp)) }
        Text("Dhanyavaad! 🔥", style = MaterialTheme.typography.displaySmall); Text("Your order ${order?.number} is in the kitchen.", style = MaterialTheme.typography.bodyLarge)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Arriving in ${order?.eta}", style = MaterialTheme.typography.titleLarge); StatusRow(true, "Order confirmed", "We received your order"); StatusRow(true, "In the kitchen", "The chefs are getting started"); StatusRow(false, "On its way", "Your courier will collect it soon") } }
        Button({ nav.navigate("orders") }, Modifier.fillMaxWidth().height(54.dp)) { Text("Track my order") }; TextButton({ nav.navigate("home") }) { Text("Back to menu") }
    }
}

@Composable private fun OrdersScreen(state: RestaurantUiState, nav: NavController) = Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { Text("Your orders", style = MaterialTheme.typography.displaySmall); if (state.order == null) EmptyState("No orders yet. Your next flame-grilled favourite is close.", "Browse menu") { nav.navigate("home") } else Card(onClick = { nav.navigate("success") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Order ${state.order.number}", style = MaterialTheme.typography.titleLarge); Text("In the kitchen", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold); Text("${state.order.lines.sumOf { it.quantity }} items · ${money(state.order.total)}"); LinearProgressIndicator(.5f, Modifier.fillMaxWidth()) } } }

@Composable private fun ProfileScreen() {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("Flavour of Fire", style = MaterialTheme.typography.displaySmall) }
        item { Image(painterResource(R.drawable.logo_fof), "Flavour of Fire logo", Modifier.size(120.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop) }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("We Made It Easy", style = MaterialTheme.typography.titleLarge)
            Text("D4, Sector 85, D Block Market,\nBeside Bank of Maharashtra,\nFaridabad, Haryana", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("North Indian · South Indian · Chinese · Beverages", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Free delivery within 3 km on orders above ₹599 · Prices exclude taxes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("+91 9971143900", "+91 9971769713").forEach { number ->
                    OutlinedButton({ context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }) {
                        Icon(Icons.Outlined.Call, null); Spacer(Modifier.width(6.dp)); Text(number)
                    }
                }
            }
        }
        items(listOf("Delivery addresses" to Icons.Outlined.LocationOn, "Dietary preferences (Veg / Non-Veg)" to Icons.Outlined.Spa, "Notifications" to Icons.Outlined.Notifications, "Help & contact" to Icons.Outlined.HelpOutline)) { (label, icon) -> ListItem(headlineContent = { Text(label) }, leadingContent = { Icon(icon, null) }, trailingContent = { Icon(Icons.Default.ChevronRight, null) }) }
        item { Text("Also available for birthday parties, kitty parties, functions and small gatherings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun Summary(state: RestaurantUiState) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { SummaryRow("Subtotal", state.subtotal); if (state.discount > 0) SummaryRow("FIRE10", -state.discount); SummaryRow("Delivery", state.delivery); HorizontalDivider(); SummaryRow("Total", state.total, true) }
@Composable private fun SummaryRow(label: String, value: Double, bold: Boolean = false) = Row(Modifier.fillMaxWidth()) { Text(label, Modifier.weight(1f), fontWeight = if (bold) FontWeight.Bold else null); Text(money(value), fontWeight = if (bold) FontWeight.Bold else null) }
@Composable private fun QuantityControl(value: Int, minus: () -> Unit, plus: () -> Unit) = Row(verticalAlignment = Alignment.CenterVertically) { IconButton(minus) { Icon(Icons.Default.Remove, "Decrease") }; Text(value.toString(), fontWeight = FontWeight.Bold); IconButton(plus) { Icon(Icons.Default.Add, "Increase") } }
@Composable private fun EmptyState(message: String, action: String, click: () -> Unit) = Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.RestaurantMenu, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary); Text(message, style = MaterialTheme.typography.titleLarge); Button(click) { Text(action) } }
@Composable private fun SectionTitle(title: String, action: String, click: () -> Unit) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f)); TextButton(click) { Text(action) } }
@Composable private fun CheckoutBlock(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, main: String, sub: String) = Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Column { Text(title, style = MaterialTheme.typography.labelLarge); Text(main, style = MaterialTheme.typography.titleMedium); Text(sub, style = MaterialTheme.typography.bodyMedium) } } }
@Composable private fun StatusRow(done: Boolean, title: String, detail: String) = Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline); Column { Text(title, fontWeight = FontWeight.Bold); Text(detail, style = MaterialTheme.typography.bodyMedium) } }
