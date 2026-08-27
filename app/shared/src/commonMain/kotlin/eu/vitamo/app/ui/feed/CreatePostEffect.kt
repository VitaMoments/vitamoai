package eu.vitamo.app.ui.feed

sealed interface CreatePostEffect {

    data object PostCreated :
        CreatePostEffect

    data object Close :
        CreatePostEffect

    data class ShowMessage(
        val message: String,
    ) : CreatePostEffect
}