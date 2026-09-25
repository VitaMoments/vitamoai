package eu.vitamo.app

import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.capabilities.UserAction

/**
 * User extension files
 */
fun AuthenticatedUser.can(
    action: UserAction
): Boolean = action in capabilities.actions