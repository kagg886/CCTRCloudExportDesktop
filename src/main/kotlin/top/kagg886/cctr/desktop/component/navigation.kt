package top.kagg886.cctr.desktop.component

import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.page.home.HOME_ROUTE
import top.kagg886.cctr.desktop.page.welcome.WELCOME_ROUTE

@Composable
fun Navigation() {
    NavigationRail {
        NavItem(
            route = HOME_ROUTE,
            title = "首页",
            icon = {
                Icon(Icons.Default.Home, "")
            }
        )
    }
}

@Composable
private fun NavItem(
    route: String,
    title: String,
    icon: @Composable (() -> Unit)
) {
    val nav = LocalNavigation.current
    val state by nav.currentEntry.collectAsState(null)
    val cur = remember(state) {
        state?.route?.route?: HOME_ROUTE
    }
    NavigationRailItem(
        selected = route == cur,
        onClick = {
            if (route != cur) {
                nav.navigate(route)
            }
        },
        label = { Text(title) },
        icon = icon
    )
}

