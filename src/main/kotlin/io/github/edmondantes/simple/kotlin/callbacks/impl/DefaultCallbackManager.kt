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

import org.slf4j.LoggerFactory
import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackExecutor
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore
import java.util.concurrent.Executor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicInteger

class DefaultCallbackManager<T>(
    scheduler: ScheduledExecutorService,
    executor: Executor? = null
) : AbstractCallbackManager<T>(LOGGER, scheduler, executor), CallbackExecutor<T>, CallbackStore<T> {

    private val nextId = AtomicInteger(1)
    private val priorityStore = PriorityCallbackStore<T>()

    override fun add(callback: Callback<T>, priority: Int, timeout: Long?): Int =
        nextId.getAndIncrement().also { id ->
            addCallback(id, priorityStore.add(id, callback, priority), timeout)
        }

    override fun invoke(data: T) {
        invoke(priorityStore.getBuckets(), data)
    }

    override fun cancel() {
        val bucket = priorityStore.getBuckets()
        priorityStore.clear()

        cancel(bucket)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultCallbackManager::class.java)
    }
}