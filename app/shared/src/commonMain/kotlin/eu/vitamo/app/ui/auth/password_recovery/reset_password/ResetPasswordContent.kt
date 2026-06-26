package eu.vitamo.app.ui.auth.password_recovery.reset_password

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ResetPasswordContent(
    state: ResetPasswordState,
    onEvent: (ResetPasswordEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Nieuw wachtwoord instellen",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "Vul je e-mailadres in en kies een nieuw wachtwoord.",
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(ResetPasswordEvent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mailadres") },
            singleLine = true,
            enabled = !state.isLoading && !state.resetCompleted,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
            ),
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(ResetPasswordEvent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nieuw wachtwoord") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isLoading && !state.resetCompleted,
        )

        OutlinedTextField(
            value = state.repeatPassword,
            onValueChange = { onEvent(ResetPasswordEvent.RepeatPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Herhaal wachtwoord") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isLoading && !state.resetCompleted,
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        state.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = { onEvent(ResetPasswordEvent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && !state.resetCompleted,
        ) {
            Text(
                text = if (state.isLoading) {
                    "Bezig..."
                } else {
                    "Wachtwoord wijzigen"
                },
            )
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        ) {
            Text(
                text = if (state.resetCompleted) {
                    "Naar login"
                } else {
                    "Terug naar login"
                },
            )
        }
    }
}