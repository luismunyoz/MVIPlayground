package com.luismunyoz.mvi

/**
 * Given a [Action] and the last [ViewState] ever rendered, the responsibility of this class
 * is to produce a new mapped [ViewState] that will be delivered to the view.
 */
interface StateMapper<Action : com.luismunyoz.mvi.Action, UiState : ViewState> {
    /**
     * Maps the provided [Action] to a [ViewState] taking into consideration the current
     * state of the UI, producing a new [ViewState] or the same [ViewState] feed to the method
     * depending on whether the UI needs to be updated or not.
     *
     * @param lastUiState the current state of the view.
     * @param action the new action that will impact in the state of the view.
     * @return if required by the conditions of the overall state, a new UiState to be
     *  pushed to the view layer. If not, the same [lastUiState].
     */
    operator fun invoke(
        action: Action,
        lastUiState: UiState,
    ): UiState
}
