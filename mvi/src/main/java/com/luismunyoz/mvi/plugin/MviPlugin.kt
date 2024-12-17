package com.luismunyoz.mvi.plugin

/**
 * MviPlugin that is executed when a [Action] is executed.
 *
 * @param Action The type of [Action] that this plugin is interested in.
 */
interface MviPlugin<Action : com.luismunyoz.mvi.Action> {
    /**
     * Called when a [Action] is executed.
     *
     * @param intentAction The [Action] that was executed.
     */
    fun onUserIntentAction(intentAction: Action)
}
