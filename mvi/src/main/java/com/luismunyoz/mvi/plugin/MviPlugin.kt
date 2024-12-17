package com.luismunyoz.mvi.plugin

import com.luismunyoz.mvi.UserIntentAction

/**
 * MviPlugin that is executed when a [UserIntentAction] is executed.
 *
 * @param Action The type of [UserIntentAction] that this plugin is interested in.
 */
interface MviPlugin<Action : UserIntentAction> {
    /**
     * Called when a [Action] is executed.
     *
     * @param intentAction The [Action] that was executed.
     */
    fun onUserIntentAction(intentAction: Action)
}
