package eu.vitamo.app.ui.auth.password_recovery.forgot_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier)
{
    Column(modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text( text = "Wachtwoord vergeten",
                style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Vul je e-mailadres in. Als er een account bestaat, sturen we een resetlink.",
                style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = state.email,
                onValueChange = {
                    onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-mailadres") },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !state.isLoading, )

        state.errorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, ) }
        state.successMessage?.let { message ->
            Text( text = message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, ) }
        Button(
            onClick = { onEvent(ForgotPasswordEvent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading, ) {
                Text(text = if (state.isLoading) { "Bezig..." } else { "Resetlink versturen" }, ) }
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading) {
            Text("Terug naar login")
        }
    }
}