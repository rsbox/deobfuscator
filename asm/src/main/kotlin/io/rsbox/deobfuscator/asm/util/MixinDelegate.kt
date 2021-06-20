package io.rsbox.deobfuscator.asm.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.ReferenceQueue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * This extension is used to provide a way to add stateful properties to objects using kotlin extension
 * methods and extension properties by storing a state value in a map
 */


/**
 * Represents a basic java weak reference type. Holds an instance
 * of a reference object with the ablility to reset this entire objects state.
 */
interface WeakReference<T> {
    val reference: T?
    fun clear()
}

interface WeakReferenceKey<K, V> : WeakReference<K> {
    val map: MutableMap<WeakReferenceKey<K, V>, V>
}

open class KotlinWeakReference<T>(value: T) : WeakReference<T> {
    internal val queue = ReferenceQueue<T>()
    private val javaWeakRef = java.lang.ref.WeakReference(value, queue)
    override val reference: T? get() = javaWeakRef.get()
    override fun clear() {
        javaWeakRef.clear()
    }
}

class KotlinWeakReferenceKey<K, V>(
    key: K,
    override val map: MutableMap<WeakReferenceKey<K, V>, V>
) : KotlinWeakReference<K>(key), WeakReferenceKey<K, V> {
    init {
        GlobalScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            queue.remove()
            map.remove(this@KotlinWeakReferenceKey)
        }
    }
}

class MixinProperty<T>(private var default: T? = null) : ReadWriteProperty<Any, T> {
    private val map = mutableMapOf<WeakReferenceKey<Any, T>, T>()

    override fun getValue(thisRef: Any, property: KProperty<*>): T = thisRef.let {
        map[map.findWeakReference(thisRef)] ?: default ?: throw NullPointerException("The mixin property has not been initialized yet.")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val key: WeakReferenceKey<Any, T> = thisRef.let {
            map.findWeakReference(thisRef) ?: KotlinWeakReferenceKey(thisRef, map)
        }

        map[key] = value
    }
}

fun <T> mixin(default: () -> T) = MixinProperty(default())

fun <T> mixin(default: T) = MixinProperty(default)

fun <T> mixin() = MixinProperty<T>(null)

fun <K, V, R : WeakReference<K>> MutableMap<R, V>.findWeakReference(key: K): R? {
    this.forEach { (currentKey, _) ->
        if(currentKey == key) {
            return currentKey
        }
    }

    return null
}