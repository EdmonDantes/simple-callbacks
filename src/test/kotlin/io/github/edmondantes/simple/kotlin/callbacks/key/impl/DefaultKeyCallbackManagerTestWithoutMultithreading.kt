/*
 * Copyright (c) 2022. Ilia Loginov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.edmondantes.simple.kotlin.callbacks.key.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackContext
import java.util.concurrent.Executors
import kotlin.random.Random

class DefaultKeyCallbackManagerTestWithoutMultithreading {

    private val callbackManager = DefaultKeyCallbackManager<Int, Any>(scheduler, null)

    @Test
    fun simpleInvokeTest() {
        val randomInt = Random.nextInt()

        val callback = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                Assertions.assertEquals(randomInt, context.data, "Income data and expected was different")
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback was cancelled")
            }
        }

        callbackManager.add(0, callback, 0)
        Thread.sleep(1000)
        callbackManager.invoke(0, randomInt)
    }

    @Test
    fun simpleKeyIsolationTest() {
        val randomInt = Random.nextInt()

        val callback = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                Assertions.fail<Any>("Callback was called")
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback was cancelled")
            }
        }

        callbackManager.add(1, callback, 0)
        Thread.sleep(1000)
        callbackManager.invoke(0, randomInt)
    }

    @Test
    fun simpleCancelTest() {
        val callback = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                Assertions.fail<Any>("Callback was invoked")
            }

            override fun cancel() {

            }
        }

        callbackManager.add(0, callback, 0)
        Thread.sleep(1000)
        callbackManager.cancel(0)
    }

    @Test
    fun testPriority() {
        val order = IntArray(2) { -1 }

        val callback1 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                order[0] = 1
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 1 was cancelled")
            }
        }

        val callback2 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                order[1] = 2
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 2 was cancelled")
            }
        }

        callbackManager.add(0, callback1, 1)
        callbackManager.add(0, callback2, 2)
        Thread.sleep(1000)
        callbackManager.invoke(0, 0)

        Assertions.assertArrayEquals(
            intArrayOf(1, 2),
            order,
            "Callback with priority 2 was called early than callback with priority 2"
        )
    }

    @Test
    fun testIgnoreNextStage() {
        val order = IntArray(2) { -1 }

        val callback1 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                order[0] = 1
                context.ignoreNextStage()
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 1 was cancelled")
            }
        }

        val callback2 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                order[1] = 2
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 2 was cancelled")
            }
        }

        callbackManager.add(0, callback1, 1)
        callbackManager.add(0, callback2, 2)
        Thread.sleep(1000)
        callbackManager.invoke(0, 0)

        Assertions.assertArrayEquals(
            intArrayOf(1, -1),
            order,
            "Callback with priority 2 was called, but manager should ignored it"
        )
    }

    @Test
    fun testMultiplyExecution() {
        val countExecutions = IntArray(2) { 0 }

        val callback1 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                countExecutions[0]++
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 1 was cancelled")
            }
        }

        val callback2 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                countExecutions[1]++
                context.remove()
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 2 was cancelled")
            }
        }

        callbackManager.add(0, callback1, 1)
        callbackManager.add(0, callback2, 2)
        Thread.sleep(1000)
        callbackManager.invoke(0, 0)
        Thread.sleep(1000)
        callbackManager.invoke(0, 1)

        Assertions.assertArrayEquals(
            intArrayOf(2, 1),
            countExecutions,
            "Callback with priority 2 was called, but manager should ignored it"
        )
    }

    @Test
    fun testCallbackIsolations() {

        var secondCallbackIsExecuted = false

        val callback1 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                error("Some error")
            }

            override fun cancel() {
                error("Some error")
            }
        }

        val callback2 = object : Callback<Any> {
            override fun execute(context: CallbackContext<Any>) {
                secondCallbackIsExecuted = true
            }

            override fun cancel() {
                Assertions.fail<Any>("Callback with priority 2 was cancelled")
            }
        }

        callbackManager.add(0, callback1, 1)
        callbackManager.add(0, callback2, 2)
        Thread.sleep(1000)
        callbackManager.invoke(0, 0)

        Assertions.assertTrue(secondCallbackIsExecuted, "Second callback wasn't called")
    }

    companion object {
        private val scheduler = Executors.newScheduledThreadPool(1)
    }

}