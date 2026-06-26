package eu.vitamo.app.ui.auth.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun VerifyEmailContent(
    email: String,
    state: VerificationState,
    onCodeChanged: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendCodeClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionsEnabled = !state.isLoading && !state.isResending

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "E-mailadres verifiëren",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "We hebben een 6-cijferige code gestuurd naar:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = actionsEnabled,
            singleLine = true,
            label = {
                Text("Verificatiecode")
            },
            placeholder = {
                Text("123456")
            },
            isError = state.codeError != null,
            supportingText = {
                state.codeError?.let {
                    Text(it)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (actionsEnabled && state.code.length == 6) {
                        onVerifyClick()
                    }
                },
            ),
        )

        state.message?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }

        state.generalError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }

        Button(
            onClick = onVerifyClick,
            enabled = actionsEnabled && state.code.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Verifiëren")
            }
        }

        TextButton(
            onClick = onResendCodeClick,
            enabled = actionsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            if (state.isResending) {
                Text("Code versturen...")
            } else {
                Text("Nieuwe code versturen")
            }
        }

        OutlinedButton(
            onClick = onBackToLoginClick,
            enabled = actionsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(
                text = "Terug naar inloggen",
                textAlign = TextAlign.Center,
            )
        }
    }
}