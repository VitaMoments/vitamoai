package eu.vitamo.app.ui.user.search

sealed interface SearchUsersEffect {

    data object ScrollToTop : SearchUsersEffect

    data class ShowMessage(
        val message: String
    ) : SearchUsersEffect
}