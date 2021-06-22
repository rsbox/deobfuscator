package io.rsbox.deobfuscator.asm.util

/**
 * Represents a JVM WeakReference wrapped object used for property mixins.
 *
 * @param T
 * @property reference T?
 */
interface WeakReference<T> {

    val reference: T?

    fun clear()

}