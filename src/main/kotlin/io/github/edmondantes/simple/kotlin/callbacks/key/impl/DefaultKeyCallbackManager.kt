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

package io.github.edmondantes.simple.kotlin.callbacks.key.impl

import org.slf4j.LoggerFactory
import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.impl.AbstractCallbackManager
import io.github.edmondantes.simple.kotlin.callbacks.impl.PriorityCallbackStore
import io.github.edmondantes.simple.kotlin.callbacks.key.KeyCallbackExecutor
import io.github.edmondantes.simple.kotlin.callbacks.key.KeyCallbackStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicInteger

class DefaultKeyCallbackManager<K, V>(
    scheduler: ScheduledExecutorService,
    executor: Executor? = null
) : AbstractCallbackManager<V>(LOGGER, scheduler, executor), KeyCallbackStore<K, V>, KeyCallbackExecutor<K, V> {

    private val nextId = AtomicInteger(1)

    private val entries = ConcurrentHashMap<K?, PriorityCallbackStore<V>>()

    override fun add(key: K, callback: Callback<V>, priority: Int, timeout: Long?): Int {
        val id = nextId.getAndIncrement()

        val bucket = entries.computeIfAbsent(key) { PriorityCallbackStore() }.add(id, callback, priority)
        addCallback(id, bucket, timeout)

        return id
    }

    override fun add(callback: Callback<V>, priority: Int, timeout: Long?): Int {
        val id = nextId.getAndIncrement()

        val bucket = entries.computeIfAbsent(null) { PriorityCallbackStore() }.add(id, callback, priority)
        addCallback(id, bucket, timeout)

        return id
    }


    override fun invoke(data: V) {
        entries[null]?.getBuckets()?.also {
            invoke(it, data)
        }
    }

    override fun invoke(key: K, data: V) {
        entries[key]?.getBuckets()?.also {
            invoke(it, data)
        }
    }

    override fun cancel() {
        entries[null]?.getBuckets()?.also {
            cancel(it)
        }
    }

    override fun cancel(key: K) {
        entries[key]?.getBuckets()?.also {
            cancel(it)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultKeyCallbackManager::class.java)
    }
}