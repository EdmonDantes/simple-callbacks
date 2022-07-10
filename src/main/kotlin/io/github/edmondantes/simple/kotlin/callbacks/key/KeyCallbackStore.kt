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

package io.github.edmondantes.simple.kotlin.callbacks.key

import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackContext
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore
import io.github.edmondantes.simple.kotlin.callbacks.SimpleCallback
import io.github.edmondantes.simple.kotlin.callbacks.future.CallbackFuture
import java.util.concurrent.CancellationException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine

/**
 * This interface expands [CallbackStore] interface, and add possibility associate callbacks and keys
 * @param K class of keys
 * @param V class of receive data
 * @see Map
 * @see Callback
 * @see CallbackStore
 */
interface KeyCallbackStore<K, V> : CallbackStore<V> {

    /**
     * Add [callback] to store and associate it with [key]
     * @param key the key to be associated with callback
     * @param callback the callback
     * @param priority the callback's priority. INT_MAX - the lowers priority, INT_MIN - the highers priority
     * @param timeout the maximum time to wait in milliseconds
     * @return callback's id
     * @see Callback
     * @see SimpleCallback
     * @see TimeUnit
     */
    fun add(
        key: K,
        callback: Callback<V>,
        priority: Int = 0,
        timeout: Long? = null
    ): Int

}

fun <K, V> KeyCallbackStore<K, V>.add(
    key: K,
    priority: Int = 0,
    timeout: Long? = null
): Future<V> {
    val future = CallbackFuture(this)
    val callback = object : Callback<V> {
        override fun execute(context: CallbackContext<V>) {
            context.remove()
            future.complete(context.data)
        }

        override fun cancel() {
            future.completeExceptionally(CancellationException("Callback was cancelled"))
        }
    }

    val callbackId = add(key, callback, priority, timeout)
    future.callbackId = callbackId
    return future
}

@Suppress("UNUSED")
@Throws(CancellationException::class)
suspend fun <K, V> KeyCallbackStore<K, V>.wait(
    key: K,
    priority: Int = 0,
    timeout: Long? = null
): V = suspendCoroutine { continuation ->
    val callback = object : Callback<V> {
        override fun execute(context: CallbackContext<V>) {
            context.remove()
            continuation.resumeWith(Result.success(context.data))
        }

        override fun cancel() {
            continuation.resumeWith(Result.failure(CancellationException("Callback was cancelled")))
        }

    }

    add(key, callback, priority, timeout)
}
