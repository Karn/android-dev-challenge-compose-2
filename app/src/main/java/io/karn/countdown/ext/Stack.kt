/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.karn.countdown.ext

import java.util.Stack

fun <T> Stack<T>.popOrNull(): T? {
    if (this.isEmpty()) {
        return null
    }

    return this.pop()
}

fun <T> stackOf(args: Collection<T>): Stack<T> {
    return Stack<T>().also { newStack -> newStack.addAll(args) }
}
