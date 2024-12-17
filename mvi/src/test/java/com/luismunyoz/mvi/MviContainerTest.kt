package com.luismunyoz.mvi

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MviContainerTest : CoroutineTest {
    override lateinit var testScope: TestScope
    override lateinit var testDispatcher: TestDispatcher

    private val initialState = TestViewState.create()
    private val intentProcessor = TestActor()
    private val stateMapper = TestStateMapper(initialState)
    private val effectProducer = TestEffectProducer()
    private lateinit var mviContainer: MviContainer<TestViewState, TestSideEffect, TestUserIntent, TestAction>

    @BeforeEach
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0

        mviContainer =
            MviContainer(
                testScope,
                intentProcessor,
                stateMapper,
                effectProducer,
                testDispatcher,
                initialState,
            )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `onUserIntent emits intent to intents flow`() =
        runTest {
            // Given
            val intent = TestUserIntent()
            val expectedViewState = TestViewState.create()
            stateMapper.configure(expectedViewState)

            // When
            mviContainer.onUserIntent(intent)

            // Then
            assertEquals(expectedViewState.ordinal, mviContainer.state.value.ordinal)
            intentProcessor.verify(intent)
        }

    @Test
    fun `new action is sent to state mapper`() =
        runTest {
            // Given
            val action = TestAction()
            intentProcessor.configure(flowOf(action))

            // When
            mviContainer.onUserIntent(TestUserIntent())

            // Then
            stateMapper.verify(initialState, action)
        }

    @Test
    fun `new action is sent to effect producer`() =
        runTest {
            // Given
            val action = TestAction()
            intentProcessor.configure(flowOf(action))

            // When
            mviContainer.onUserIntent(TestUserIntent())

            // Then
            effectProducer.verify(initialState, action)
        }

    @Test
    fun `new view state is pushed properly`() =
        runTest {
            // Given
            val action = TestAction()
            val expectedViewState = TestViewState.create()
            intentProcessor.configure(flowOf(action))
            stateMapper.configure(expectedViewState)

            // When
            mviContainer.onUserIntent(TestUserIntent())

            // Then
            assertEquals(expectedViewState.ordinal, mviContainer.state.value.ordinal)
        }

    @Test
    fun `new side effect is pushed properly`() =
        runTest {
            // Given
            val action = TestAction()
            val expectedSideEffect = TestSideEffect()
            intentProcessor.configure(flowOf(action))
            effectProducer.configure(expectedSideEffect)

            // When
            mviContainer.onUserIntent(TestUserIntent())

            // Then
            assertEquals(expectedSideEffect, mviContainer.sideEffect.first())
        }

    private class TestUserIntent : UserIntent

    private class TestAction : Action

    private data class TestViewState(
        val ordinal: Int,
    ) : ViewState {
        companion object {
            private var counter = 0

            fun create() = TestViewState(counter++)
        }
    }

    private class TestSideEffect : SideEffect

    private class TestActor : Actor<TestUserIntent, TestAction> {
        private var lastIntentReceived: TestUserIntent? = null
        private var responseFlow: Flow<TestAction> = flowOf(TestAction())

        override suspend fun invoke(intent: TestUserIntent): Flow<TestAction> {
            lastIntentReceived = intent
            return responseFlow
        }

        fun configure(responseFlow: Flow<TestAction>) {
            this.responseFlow = responseFlow
        }

        fun verify(intent: TestUserIntent) {
            assertEquals(intent, lastIntentReceived)
        }
    }

    private class TestStateMapper(
        initialState: TestViewState,
    ) : StateMapper<TestAction, TestViewState> {
        private var lastUiStateReceived: TestViewState? = null
        private var userIntentActionReceived: TestAction? = null
        private var returnState: TestViewState = initialState

        override fun invoke(
            action: TestAction,
            lastUiState: TestViewState,
        ): TestViewState {
            lastUiStateReceived = lastUiState
            userIntentActionReceived = action
            return returnState
        }

        fun configure(returnState: TestViewState) {
            this.returnState = returnState
        }

        fun verify(
            lastUiState: TestViewState,
            lastIntent: TestAction,
        ) {
            assertEquals(lastUiState, lastUiStateReceived)
            assertEquals(lastIntent, userIntentActionReceived)
        }
    }

    private class TestEffectProducer(
        sideEffect: TestSideEffect? = null,
    ) : EffectProducer<TestAction, TestViewState, TestSideEffect> {
        private var returnSideEffect: TestSideEffect? = sideEffect
        private var lastActionReceived: TestAction? = null
        private var lastUiStateReceived: TestViewState? = null

        override fun invoke(
            action: TestAction,
            uiState: TestViewState,
        ): TestSideEffect? {
            lastActionReceived = action
            lastUiStateReceived = uiState
            return returnSideEffect
        }

        fun configure(returnSideEffect: TestSideEffect) {
            this.returnSideEffect = returnSideEffect
        }

        fun verify(
            uiState: TestViewState,
            action: TestAction,
        ) {
            assertEquals(action, lastActionReceived)
            assertEquals(uiState, lastUiStateReceived)
        }
    }
}
