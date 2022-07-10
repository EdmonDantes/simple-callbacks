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

package io.github.edmondantes.simple.kotlin.callbacks.coroutines

import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackContext
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore
import java.util.concurrent.CancellationException
import kotlin.coroutines.suspendCoroutine

/**
 * This method helps to work with callbacks in coroutines
 * @see CallbackStore.add
 */
@Suppress("UNUSED")
@Throws(CancellationException::class)
suspend fun <T> CallbackStore<T>.wait(
    priority: Int = 0,
    timeout: Long? = null,
): T =
    suspendCoroutine { continuation ->
        val callback = object : Callback<T> {
            override fun execute(context: CallbackContext<T>) {
                context.remove()
                continuation.resumeWith(Result.success(context.data))
            }

            override fun cancel() {
                continuation.resumeWith(Result.failure(CancellationException("Callback was cancelled")))
            }

        }

        add(callback, priority, timeout)
    }
