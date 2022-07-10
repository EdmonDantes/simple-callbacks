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

package io.github.edmondantes.simple.kotlin.callbacks.impl

import io.github.edmondantes.simple.kotlin.callbacks.Callback
import java.util.TreeMap

class PriorityCallbackStore<T> {

    private val priority: MutableMap<Int, CallbackBucket<T>> = TreeMap()

    fun add(id: Int, callback: Callback<T>, priority: Int): CallbackBucket<T> {
        val bucket = synchronized(this.priority) {
            this.priority.computeIfAbsent(priority) { CallbackBucket() }
        }

        synchronized(bucket) {
            bucket.add(id, callback)
        }

        return bucket
    }

    operator fun get(priority: Int): CallbackBucket<T>? =
        synchronized(this.priority) {
            this.priority[priority]
        }

    fun getBuckets(): List<CallbackBucket<T>> =
        synchronized(priority) {
            ArrayList(priority.values)
        }

    fun clear() {
        synchronized(priority) {
            priority.clear()
        }
    }

}