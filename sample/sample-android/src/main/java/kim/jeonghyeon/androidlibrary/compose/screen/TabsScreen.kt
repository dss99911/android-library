package kim.jeonghyeon.androidlibrary.compose.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.VectorAsset
import kim.jeonghyeon.androidlibrary.compose.Screen

abstract class TabsScreen : Screen() {
    /**
     * Icon, Screen
     * todo make readable structure.
     */
    abstract val tabs: List<Pair<VectorAsset?, Screen>>
    abstract val initialIndex: Int
    var tabIndex by mutableStateOf(initialIndex)
    var currentTab
        get() = tabs[tabIndex].second
        set(tab: Screen) {
            tabIndex = tabs.indexOfFirst { it.second === tab }
        }
}