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

class CallbackBucket<T> : Collection<Callback<T>> {
    private val nodeIds = HashMap<Int, CallbackBucketNode<T>>()
    private var first: CallbackBucketNode<T>? = null
    private var last: CallbackBucketNode<T>? = null

    override val size: Int
        get() = nodeIds.size

    fun add(id: Int, callback: Callback<T>) {
        val node = CallbackBucketNode(id, callback)
        nodeIds[id] = node
        if (last == null) {
            first = node
            last = node
            return
        }

        last!!.next = node
        node.prev = last!!
        last = node
    }


    override fun contains(element: Callback<T>): Boolean {
        val iter = iterator()
        while (iter.hasNext()) {
            val obj = iter.next()
            if (obj == element) {
                return true
            }
        }
        return false
    }

    override fun containsAll(elements: Collection<Callback<T>>): Boolean =
        elements.all { contains(it) }


    override fun isEmpty(): Boolean =
        nodeIds.isEmpty()

    override fun iterator(): MutableIterator<Callback<T>> =
        CallbackBucketIterator()

    fun clear() {
        nodeIds.clear()
        first = null
        last = null
    }

    fun remove(id: Int): Callback<T>? {
        val node = nodeIds.remove(id) ?: return null
        if (first === node) {
            first = node.next
        }

        if (last === node) {
            last = node.prev
        }

        node.remove()

        return node.callback
    }

    private class CallbackBucketNode<T>(val id: Int, val callback: Callback<T>) {
        var prev: CallbackBucketNode<T>? = null
        var next: CallbackBucketNode<T>? = null

        fun remove() {
            prev?.also { prev ->
                prev.next = next
            }
            next?.also { next ->
                next.prev = prev
            }
            prev = null
            next = null
        }
    }

    private inner class CallbackBucketIterator : MutableIterator<Callback<T>> {
        private var node: CallbackBucketNode<T>? = null
        private var next: CallbackBucketNode<T>? = first

        override fun hasNext(): Boolean = next != null

        override fun next(): Callback<T> {
            if (!hasNext()) {
                throw NoSuchElementException()
            }

            node = next
            next = next?.next
            return node!!.callback
        }

        override fun remove() {
            if (node == null) {
                throw NoSuchElementException()
            }

            remove(node!!.id)
            node = null
        }

    }
}



