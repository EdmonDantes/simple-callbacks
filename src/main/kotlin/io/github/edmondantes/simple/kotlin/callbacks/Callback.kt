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
 * This interface describes a callback. If callback was cancelled, it will not call anymore
 * @param T class of receive data
 * @see CallbackStore
 * @see CallbackExecutor
 * @see CallbackContext
 * @see SimpleCallback
 */
interface Callback<T> {
    /**
     * This method will be called if an action finished success.
     * If this method finished with exception, callback will be removed from queue
     *
     * @param context the callback context
     * @see CallbackContext
     */
    @Throws(Exception::class)
    fun execute(context: CallbackContext<T>)

    /**
     * This method will be called before callback will be deleted from queue
     */
    @Throws(Exception::class)
    fun cancel()
}