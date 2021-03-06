package io.karn.countdown.ext

import java.util.*

fun <T> Stack<T>.popOrNull(): T? {
    if (this.isEmpty()) {
        return null
    }

    return this.pop()
}

fun <T> stackOf(args: Collection<T>): Stack<T> {
    return Stack<T>().also { newStack -> newStack.addAll(args) }
}