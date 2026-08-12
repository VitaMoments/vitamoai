package eu.vitamo.app.infrastructure.permissions.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationPermissionViewModel(
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        NotificationPermissionState(),
    )

    val state = _state.asStateFlow()

    private val _effects = Channel<NotificationPermissionEffect>(
        capacity = Channel.BUFFERED,
    )

    val effects = _effects.receiveAsFlow()

    init {
        refresh()
    }

    fun onEvent(
        event: NotificationPermissionEvent,
    ) {
        when (event) {
            NotificationPermissionEvent.Refresh -> {
                refresh()
            }

            NotificationPermissionEvent.RequestPermission -> {
                requestPermission()
            }

            NotificationPermissionEvent.OpenSettings -> {
                permissionManager.openSettings()
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                )
            }

            try {
                val status = permissionManager.check(
                    permission = AppPermission.NOTIFICATIONS,
                )

                _state.update {
                    it.copy(
                        status = status,
                        isLoading = false,
                    )
                }
            } catch (exception: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                    )
                }

                _effects.send(
                    NotificationPermissionEffect.ShowMessage(
                        message = "Notificatiestatus kon niet worden gecontroleerd.",
                    ),
                )
            }
        }
    }

    private fun requestPermission() {
        if (_state.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                )
            }

            try {
                val status = permissionManager.request(
                    permission = AppPermission.NOTIFICATIONS,
                )

                _state.update {
                    it.copy(
                        status = status,
                        isLoading = false,
                    )
                }

                if (
                    status == PermissionStatus.GRANTED ||
                    status == PermissionStatus.PARTIAL_GRANTED
                ) {
                    _effects.send(
                        NotificationPermissionEffect.PermissionGranted,
                    )
                }
            } catch (exception: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                    )
                }

                _effects.send(
                    NotificationPermissionEffect.ShowMessage(
                        message = "Notificatietoestemming kon niet worden aangevraagd.",
                    ),
                )
            }
        }
    }
}