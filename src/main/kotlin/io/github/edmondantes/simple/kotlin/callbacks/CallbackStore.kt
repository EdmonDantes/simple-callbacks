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

package io.github.edmondantes.simple.kotlin.callbacks

import java.util.concurrent.TimeUnit

/**
 * This interface describes an object which can store callbacks for future using
 * @param T class of receive data
 * @see Callback
 * @see SimpleCallback
 * @see CallbackExecutor
 */
interface CallbackStore<T> {
    /**
     * Add new [callback] to store.
     *
     * If timeout is set, callback will be cancelled after this time, if it doesn't invoke.
     * If callback was called, timeout will be calculated from last call
     *
     * @param callback the callback
     * @param priority the callback's priority. INT_MAX - the lowers priority, INT_MIN - the highers priority
     * @param timeout the maximum time to wait in milliseconds
     * @return callback's id
     * @see Callback
     * @see SimpleCallback
     * @see TimeUnit
     */
    fun add(
        callback: Callback<T>,
        priority: Int = 0,
        timeout: Long? = null,
    ): Int

    /**
     * Remove callback by [callbackId]
     * @param callbackId callback's id
     */
    fun remove(callbackId: Int)
}