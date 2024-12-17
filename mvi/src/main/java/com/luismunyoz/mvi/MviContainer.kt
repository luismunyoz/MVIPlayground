package com.luismunyoz.mvi

import android.util.Log
import com.luismunyoz.mvi.plugin.MviPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Container of the MVI pieces. This class takes care of managing the interaction between the
 * different components of the library and ensuring that state changes are communicated properly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
class MviContainer<UiState : ViewState, Effect : SideEffect, Intent : UserIntent, Action : UserIntentAction>(
    private val coroutineScope: CoroutineScope,
    private val intentProcessor: IntentProcessor<Intent, Action>,
    private val stateMapper: StateMapper<Action, UiState>,
    private val effectProducer: EffectProducer<Action, UiState, Effect>,
    processDispatcher: CoroutineDispatcher = Dispatchers.IO,
    initialState: UiState,
    plugins: List<MviPlugin<Action>> = emptyList(),
) {
    private val intents = MutableSharedFlow<Intent>()

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<UiState> = _state

    private val _sideEffect = Channel<Effect>(Channel.BUFFERED)
    val sideEffect: Flow<Effect> = _sideEffect.receiveAsFlow()

    init {
        intents
            .onEach { intent -> Log.v(LOG_TAG, "(1) Start Processing User Intent = $intent") }
            .flatMapMerge { intent -> intentProcessor(intent) }
            .onEach { intentAction ->
                plugins.forEach {
                    it.onUserIntentAction(intentAction)
                }
            }.map { intentAction ->
                val currentUiState = _state.value
                val nextUiState = stateMapper(intentAction, currentUiState)
                val nextSideEffect = effectProducer(intentAction, currentUiState)
                Pair(nextUiState, nextSideEffect)
            }.onEach { produced ->
                Log.v(LOG_TAG, "(3) Produced is $produced")
                produced.first.let { newUiState ->
                    _state.value = newUiState
                }

                produced.second?.let { newSideEffect ->
                    _sideEffect.send(newSideEffect)
                }
            }.flowOn(processDispatcher)
            .launchIn(coroutineScope)
    }

    fun onUserIntent(intent: Intent) {
        coroutineScope.launch { intents.emit(intent) }
    }
}
