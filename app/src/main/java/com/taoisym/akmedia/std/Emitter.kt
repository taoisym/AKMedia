package com.taoisym.akmedia.std

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

open class Emitter<T>(private var value: T, private val pass: ((T) -> Unit)?) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
        this::value.setter
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        pass?.invoke(value)
        this.value = value
    }

}

inline fun <T> emitter(init: T): Emitter<T> {
    return Emitter(init, null)
}

fun <T> emitter(init: T, pass: ((T) -> Unit)?): Emitter<T> {
    return Emitter(init, pass)
}

fun <T> emitter(init: T, pass: KMutableProperty0<T>?): Emitter<T> {
    return Emitter<T>(init, { pass?.set(it) })
}

fun <T> emitter(init: () -> T, pass: ((T) -> Unit)?): Emitter<T> {
    return Emitter(init(), pass)
}