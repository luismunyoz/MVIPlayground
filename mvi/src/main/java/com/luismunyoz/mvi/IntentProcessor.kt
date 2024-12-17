package com.luismunyoz.mvi

import kotlinx.coroutines.flow.Flow

/**
 * Takes care of processing a [UserIntent] by interacting with the the Model/Application. This
 * interaction will produce [UserIntentAction] that will, in time, update the state of the view.
 */
interface IntentProcessor<Intent : UserIntent, Action : UserIntentAction> {
    /**
     * Processes the provided [intent] and produces a stream that will emmit every
     * state change.
     *
     * @param intent the UserIntent to process.
     * @return a [Flow] that will emit [UserIntentAction] as part of the processing of
     * the [UserIntent].
     */
    suspend operator fun invoke(intent: Intent): Flow<Action>
}