package com.luismunyoz.mvi

import androidx.annotation.RestrictTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

@ExtendWith(CoroutineTestExtension::class)
interface CoroutineTest {
    var testScope: TestScope
    var testDispatcher: TestDispatcher
}

/**
 * JUnit 5 extension used to replace the Main dispatcher (Coroutines) by a custom CoroutineDispatcher
 * that can be used in the JVM tests.
 *
 * Usage:
 * class MyTest : CoroutineTest {
 *
 *   override lateinit var testScope: TestCoroutineScope
 *   override lateinit var testDispatcher: TestCoroutineDispatcher
 *
 * }
 */
@RestrictTo(RestrictTo.Scope.TESTS)
internal class CoroutineTestExtension :
    TestInstancePostProcessor,
    BeforeAllCallback,
    AfterEachCallback,
    AfterAllCallback {
    // This Dispatcher is injected for usage in a test class.
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    // This Scope is injected for usage in a test class.
    private val testScope = TestScope(testDispatcher)

    override fun postProcessTestInstance(
        testInstance: Any?,
        context: ExtensionContext?,
    ) {
        (testInstance as? CoroutineTest)?.let { coroutineTest ->
            coroutineTest.testScope = testScope
            coroutineTest.testDispatcher = testDispatcher
        }
    }

    override fun beforeAll(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) = Unit

    override fun afterAll(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}
