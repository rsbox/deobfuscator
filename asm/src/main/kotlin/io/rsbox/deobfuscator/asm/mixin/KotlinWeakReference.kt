package io.rsbox.deobfuscator.asm.util

import java.lang.ref.ReferenceQueue

open class KotlinWeakReference<T>(value: T) : WeakReference<T> {

    internal val queue = ReferenceQueue<T>()

    private val wrappedWakeReference = java.lang.ref.WeakReference(value, queue)

    override val reference: T? get() = wrappedWakeReference.get()

    override fun clear() {
        wrappedWakeReference.clear()
    }

}