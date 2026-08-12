package eu.vitamo.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(
   accessToken: String? ,
   refreshToken: String?
) {
    Column {

        Text(text = accessToken ?: "access token empty")
        HorizontalDivider()
        Text(text = refreshToken ?: "refresh token empty")
    }
}