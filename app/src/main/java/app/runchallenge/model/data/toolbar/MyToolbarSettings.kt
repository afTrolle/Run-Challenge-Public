package app.runchallenge.model.data.toolbar

import app.runchallenge.R

data class MyToolbarSettings(
    val isVisible: Boolean = true,
    val title: Int = R.string.app_name,
    val showBackButton: Boolean = false,
    val optionsMenu: Int? = null,
    val elevation: Float = 4f
) {
    //return true if has options menu declared
    fun hasOptionsMenu(): Boolean = optionsMenu?.let { true } ?: false
}