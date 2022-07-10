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

/**
 * This interface describes an object which can execute callbacks
 * @param T class of receive data
 * @see Callback
 * @see SimpleCallback
 * @see CallbackStore
 */
interface CallbackExecutor<T> {

    /**
     * Invoke all callbacks with received [data]
     * @param data received data
     */
    fun invoke(data: T)

    /**
     * Cancel all callbacks
     */
    fun cancel()

}