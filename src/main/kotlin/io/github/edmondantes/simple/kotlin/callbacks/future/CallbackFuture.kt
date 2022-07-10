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

package io.github.edmondantes.simple.kotlin.callbacks.future

import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackContext
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore
import io.github.edmondantes.simple.kotlin.callbacks.SimpleCallback
import java.util.concurrent.CancellationException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * This class provides integration between java concurrent and simple callbacks
 * @see Callback
 * @see SimpleCallback
 * @see CallbackStore
 */
class CallbackFuture<T>(private val callbackStore: CallbackStore<T>) : Future<T> {

    private val lock = Any()
    private var state: Int = 0
    private var result: T? = null
    private var exception: Throwable? = null

    internal var callbackId: Int? = null

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (callbackId == null) {
            return false
        }

        synchronized(lock) {
            callbackStore.remove(callbackId!!)
            state = CANCELED_STATE
        }
        return true
    }

    override fun isCancelled(): Boolean = synchronized(lock) { state == CANCELED_STATE }

    override fun isDone(): Boolean = synchronized(lock) { state == DONE_STATE }

    override fun get(): T {
        while (synchronized(lock) { state == 0 }) {
            Thread.sleep(1)
        }

        return getNow()
    }

    override fun get(timeout: Long, unit: TimeUnit): T {
        val timeoutTime = System.currentTimeMillis() + unit.toMillis(timeout)
        while (synchronized(lock) { state == 0 } && timeoutTime < System.currentTimeMillis()) {
            Thread.sleep(1)
        }

        if (synchronized(lock) { state == 0 }) {
            throw TimeoutException()
        }

        return getNow()
    }

    /**
     * This method will be called if action finished success
     */
    fun complete(result: T) {
        synchronized(lock) {
            if (state != 0) {
                return
            }
            state = DONE_STATE
            this.result = result
        }
    }

    /**
     * This method will be called, if something goes wrong
     */
    fun completeExceptionally(exception: Throwable) {
        synchronized(lock) {
            if (state != 0) {
                return
            }
            state = EXCEPTION_STATE
            this.exception = exception
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getNow(): T {
        when (synchronized(lock) { state }) {
            CANCELED_STATE ->
                throw IllegalStateException("Future was cancelled by user")
            DONE_STATE ->
                return result as T
            EXCEPTION_STATE ->
                throw (exception ?: IllegalStateException("Callback was finished with exception"))
            else ->
                throw IllegalStateException("Can not get result now. Illegal future's state for receiving result")
        }
    }

    companion object {
        const val CANCELED_STATE = 1
        const val DONE_STATE = 2
        const val EXCEPTION_STATE = 3
    }
}

@Suppress("UNUSED")
fun <T> CallbackStore<T>.future(
    priority: Int = 0,
    timeout: Long? = null
): Future<T> {
    val future = CallbackFuture(this)
    val callback = object : Callback<T> {
        override fun execute(context: CallbackContext<T>) {
            context.remove()
            future.complete(context.data)
        }

        override fun cancel() {
            future.completeExceptionally(CancellationException("Callback was cancelled"))
        }
    }

    val callbackId = add(callback, priority, timeout)
    future.callbackId = callbackId
    return future
}