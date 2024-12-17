package com.luismunyoz.mvi

/**
 * Represents the state of the view at any given moment.
 *
 * Every time there is a state change, the library will deliver a new instance of this ViewState
 * so the View can render the new state as expected.
 */
interface ViewState

/**
 * Represents a side effect that is triggered as a result of a state change. This effect is a
 * one time event that indicates a change in the state of the application that is not permanent
 * and, as such, it should be consumed and never triggered again.
 */
interface SideEffect

/**
 * Represents the intention of the user to perform an action. Every action from the user that has
 * a change in the state of the application must have an equivalent [UserIntent]. This UserIntent
 * will be delivered to the library from the View and the library will deliver the resulting [Action]
 * change as a [ViewState] that the View can process and render.
 */
interface UserIntent

/**
 * Represents an action that needs to be routed in result to a [UserIntent]. This action will indicate
 * the transformations that needs to be applied to the [ViewState] and/or the [SideEffect] in order
 * to reflect the intent that the user has executed.
 */
interface Action
