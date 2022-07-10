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

import kotlin.jvm.Throws

/**
 * This interface describes simple callback.
 * @param T class of receive data
 * @see Callback
 * @see CallbackStore
 * @see CallbackExecutor
 */
@FunctionalInterface
fun interface SimpleCallback<T> : Callback<T> {

    /**
     * This method will be called if callback change any state.
     * If callback will be cancelled [context] will be null
     *
     * @param context the callback context. If it is null, then callback is cancelled
     */
    @Throws(Exception::class)
    fun simpleExecute(context: CallbackContext<T>?)

    override fun execute(context: CallbackContext<T>) {
        simpleExecute(context)
    }

    override fun cancel() {
        simpleExecute(null)
    }
}