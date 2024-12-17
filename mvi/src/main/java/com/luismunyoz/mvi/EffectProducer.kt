package com.luismunyoz.mvi

/**
 * Given a [Action] and the last [UiState] ever rendered, it is responsibility of this class
 * to produce a new [SideEffect] (or not) that will be delivered to the View.
 */
interface EffectProducer<Action : com.luismunyoz.mvi.Action, UiState : ViewState, Effect : SideEffect> {
    /**
     * Produces a new [SideEffect] taking into account the new state of the UI and the
     * [Action] of a user intention.
     * It might or might not produce a new [SideEffect] if the conditions of the state
     * requires so.
     *
     * @param uiState current state of the view.
     * @param action the last UserIntentAction emitted by the system.
     * @return if required by the conditions of the overall state, a new [SideEffect] to be
     *  pushed to the view layer. Null otherwise.
     */
    operator fun invoke(
        action: Action,
        uiState: UiState,
    ): Effect?
}
