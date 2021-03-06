package io.karn.countdown.ext

import androidx.compose.ui.Modifier

fun Modifier.whenTrue(predicate: Boolean, apply: Modifier.() -> Unit): Modifier {
    if (predicate) {
        apply(this)
    }

    return this
}