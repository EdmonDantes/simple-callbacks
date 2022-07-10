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

import org.slf4j.Logger
import io.github.edmondantes.simple.kotlin.callbacks.Callback
import io.github.edmondantes.simple.kotlin.callbacks.CallbackContext
import io.github.edmondantes.simple.kotlin.callbacks.CallbackExecutor
import io.github.edmondantes.simple.kotlin.callbacks.CallbackStore
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class AbstractCallbackManager<T>(
    private val logger: Logger,
    private val scheduler: ScheduledExecutorService? = null,
    private val executor: Executor? = null
) : CallbackStore<T>, CallbackExecutor<T> {

    private val callbacks: MutableMap<Int, CallbackBucket<T>> = ConcurrentHashMap()

    override fun remove(callbackId: Int) {
        val bucket = callbacks.remove(callbackId) ?: return

        val callback = synchronized(bucket) {
            bucket.remove(callbackId)
        }

        if (callback != null) {
            cancelCallback(callback)
        }
    }

    protected open fun addCallback(id: Int, bucket: CallbackBucket<T>, timeout: Long?) {
        callbacks[id] = bucket
        if (timeout != null && scheduler != null && timeout > 0) {
            scheduler.schedule({ remove(id) }, timeout, TimeUnit.MILLISECONDS)
        }
    }

    protected open fun invoke(buckets: Iterable<CallbackBucket<T>>, data: T) {
        for (callbackBucket in buckets) {
            var needToIgnoreNextStage = false
            synchronized(callbackBucket) {
                if (executor != null) {
                    val futureCallbackContexts = ArrayList<Future<DefaultCallbackContext<T>>>(callbackBucket.size)

                    for (callback in callbackBucket) {
                        val future = executor.submit {
                            DefaultCallbackContext(data).also { context ->
                                executeCallback(callback, context)
                            }
                        }

                        futureCallbackContexts.add(future)
                    }

                    val callbackContexts = futureCallbackContexts.map { it.get() }

                    val callbackIdsIterator = callbackBucket.iterator()
                    val callbackContextsIterator = callbackContexts.iterator()

                    while (callbackIdsIterator.hasNext() && callbackContextsIterator.hasNext()) {
                        callbackIdsIterator.next()
                        val context = callbackContextsIterator.next()

                        if (context.isMarkRemove) {
                            callbackIdsIterator.remove()
                        }

                        if (context.isMarkIgnoreNextStage) {
                            needToIgnoreNextStage = true
                        }
                    }
                } else {
                    val bucketIterator = callbackBucket.iterator()
                    while (bucketIterator.hasNext()) {
                        val callback = bucketIterator.next()
                        val context = DefaultCallbackContext(data)

                        executeCallback(callback, context)

                        if (context.isMarkRemove) {
                            bucketIterator.remove()
                        }

                        if (context.isMarkIgnoreNextStage) {
                            needToIgnoreNextStage = true
                        }
                    }
                }
            }

            if (needToIgnoreNextStage) {
                break
            }
        }
    }

    protected open fun executeCallback(callback: Callback<T>, context: CallbackContext<T>) {
        try {
            callback.execute(context)
        } catch (e: Exception) {
            logger.error(
                "Can not execute method 'execute' in callback. It will be removed from queue",
                e
            )
            try {
                callback.cancel()
            } catch (e: Exception) {
                logger.error("Can not execute method 'cancel' in callback", e)
            }
            context.remove()
        }
    }

    protected open fun cancel(buckets: Iterable<CallbackBucket<T>>) {
        buckets.forEach { bucket ->
            synchronized(bucket) {
                for (callback in bucket) {
                    cancelCallback(callback)
                }
                bucket.clear()
            }
        }
    }

    protected open fun cancelCallback(callback: Callback<T>) {
        val tryCatchFunc = {
            try {
                callback.cancel()
            } catch (e: Exception) {
                logger.error("Can not successfully cancel callback", e)
            }
        }

        executor?.execute(tryCatchFunc) ?: tryCatchFunc()
    }

    private fun <T> Executor.submit(callable: Callable<T>): Future<T> = FutureTask(callable).also { execute(it) }
}