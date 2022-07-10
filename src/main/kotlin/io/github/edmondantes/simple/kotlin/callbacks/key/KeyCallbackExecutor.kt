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
import io.github.edmondantes.simple.kotlin.callbacks.CallbackExecutor
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore

/**
 * This interface expands [CallbackExecutor] interface, and add possibility call callbacks for associated keys
 * @param K class of keys. Required implements [Any.hashCode] and [Any.equals]
 * @param V class of receive data
 * @see Map
 * @see Callback
 * @see CallbackStore
 */
interface KeyCallbackExecutor<K, V> : CallbackExecutor<V> {

    /**
     * Invoke all callbacks witch associated with [key] with received [data]
     * @param key the key which associated with callbacks
     * @param data received data
     */
    fun invoke(key: K, data: V)

    /**
     * Cancel all callback witch associated with [key]
     * @param key the key which associated with callbacks
     */
    fun cancel(key: K)

}