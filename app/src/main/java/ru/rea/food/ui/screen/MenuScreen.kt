package ru.rea.food.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ru.rea.food.Product
import ru.rea.food.R
import ru.rea.food.ui.MissingDialog
import ru.rea.food.ui.item.CategoryItem
import ru.rea.food.ui.item.ProductItem
import ru.rea.food.ui.topbar.TopAppBar
import ru.rea.food.vm.MenuViewModel
import ru.rea.food.vm.MenuViewModelFactory

private const val TAG = "MenuScreen"

@ExperimentalMaterialApi
@Composable
fun MenuScreen(place: Int, token: String, nav: NavController) {
    val viewModel = viewModel<MenuViewModel>(factory = MenuViewModelFactory(token))

    var product by remember { mutableStateOf<Product?>(null) }
    var navItem by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf(0) }

    val dishesState = rememberLazyListState()
    val catsState = rememberLazyListState()

    when (navItem) {
        0 -> product?.let {
            ProductScreen(it, token) { product = null }
        } ?: run {
            Scaffold(
                topBar = {
                    TopAppBar(onClick = {
                        nav.navigate("appMenu")
                    },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_group_399),
                                contentDescription = null
                            )
                        })
                },
                bottomBar = {
                    val top = 32.dp
                    val shape = RoundedCornerShape(top, top)
                    BottomNavigation(
                        modifier = Modifier
                            .clip(shape)
                            .height(96.dp),
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        val icons = listOf(
                            R.drawable.ic_home to R.drawable.ic_sel_home,
                            R.drawable.ic_heart to R.drawable.ic_sel_heart,
                            null to null,
                            R.drawable.ic_notification to R.drawable.ic_sel_notification,
                            R.drawable.ic_buy to R.drawable.ic_sel_buy
                        )
                        repeat(5) {
                            BottomNavigationItem(
                                selected = navItem == it,
                                onClick = {
                                    if (it != 2)
                                        navItem = it
                                    if (it == 4) // мда
                                        viewModel.cart()
                                },
                                icon = {
                                    (if (navItem == it) icons[it].second else icons[it].first)?.let {
                                        Image(
                                            painter = painterResource(id = it),
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            Log.d(TAG, "MenuScreen: search")
                            navItem = 2
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
                isFloatingActionButtonDocked = true
            ) {
                Column(
                    Modifier
                        .background(MaterialTheme.colors.background)
                        .fillMaxSize()
                        .padding(it)
                        .padding(horizontal = dimensionResource(id = R.dimen.content_padding))
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.enjoy),
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    viewModel.products(place, category)
                    viewModel.categories(place)
                    if (viewModel.categories.isEmpty()) {
                        LinearProgressIndicator()
                    } else {
                        LazyRow(Modifier.weight(0.5f), state = catsState)
                        {
                            items(viewModel.categories) { cat ->
                                CategoryItem(cat, cat.id == category) {
                                    category = if (category == it) 0 else it
                                    viewModel.products(place, category)
                                }
                            }
                        }
                    }
                    val missing = stringResource(id = R.string.missing)
                    var missingDialog by remember { mutableStateOf(false) }
                    if (missingDialog) {
                        MissingDialog(missing) {
                            missingDialog = false
                        }
                    }
                    LazyRow(Modifier.weight(1f), state = dishesState)
                    {
                        items(viewModel.products) {
                            ProductItem(it) {
                                if (it.desc == missing) {
                                    missingDialog = true
                                } else {
                                    product = it
                                }
                            }
                        }
                    }
                }
            }
        }
        1 -> RecentProductsScreen(token) {
            product = it
            navItem = 0
        }
        2 -> SearchScreen(viewModel) {
            product = it
            navItem = 0
        }
        3 -> Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Уведомления", style = MaterialTheme.typography.h5)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "У вас пока нет уведомлений", style = MaterialTheme.typography.h5)
            }
        }
        4 -> CartScreen(viewModel, {
            nav.navigate("checkout")
        }) { navItem = 0 }
    }
}