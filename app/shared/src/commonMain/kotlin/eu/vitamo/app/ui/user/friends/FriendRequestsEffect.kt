package eu.vitamo.app.ui.user.friends

sealed interface FriendRequestsEffect {

    data object ScrollToTop : FriendRequestsEffect

    data class ShowMessage(
        val message: String
    ) : FriendRequestsEffect
}