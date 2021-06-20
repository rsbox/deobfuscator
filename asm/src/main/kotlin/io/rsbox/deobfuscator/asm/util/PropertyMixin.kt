package io.rsbox.deobfuscator.asm.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private fun <K, V, T : WeakReference<K>> MutableMap<T, V>.findWeakReferenceForKey(key: K) : T? {
    for((currentKey, _) in this) {
        if(currentKey.reference == key) {
            return currentKey
        }
    }

    return null
}

/**
 * The Mixin DSL global method for delegation.
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<PropertyMixin<T>, Unit>
 * @return PropertyMixin<T>
 */
fun <T, V> mixin(init: T? = null): PropertyMixin<T, V> {
    return PropertyMixin(init)
}

fun <T, V> nullableMixin(): SingleNullPropertyMixin<T, V> {
    return SingleNullPropertyMixin()
}

/**
 * Represents a Kotlin mixin'd property using kotlin extension methods by wrapper
 * the JVM weak referenced types with a kotlin coroutine backed garbage collector.
 *
 * Using this with a kotlin extension property getter and server will appear in kotlin source
 * code as a field which has been dynamically added to a class. This of course is not the case since there is no
 * injection happening.
 *
 * The kotlin coroutine GC provides this solution with almost zero overhead. However, this means this implementation
 * is not well suited for long-live instances of the runtime or for storing LARGE amounts of data since all data is stored
 * in memory when using property mixins.
 *
 * Also acts as a DSL for additional features such as data binding and bind aware object transforms.
 *
 * @param T
 */
class PropertyMixin<T, V>(private val init: T?) : ReadWriteProperty<V, T> {

    private val map = mutableMapOf<WeakKeyedReference<Any, T>, T>()

    /**
     * Gets the mixin value from the reference map if it is initialized.
     *
     * @param thisRef Any
     * @param property KProperty<*>
     * @return T
     */
    override fun getValue(thisRef: V, property: KProperty<*>): T = thisRef?.let {
        map[map.findWeakReferenceForKey(thisRef)]
    } ?: (init
        ?: throw UninitializedPropertyAccessException("Unable to get the value of the mixin property as it has not been initialized."))

    /**
     * Sets and/or stores a value in the reference map  for a mixin property.
     *
     * @param thisRef Any
     * @param property KProperty<*>
     * @param value T
     */
    override fun setValue(thisRef: V, property: KProperty<*>, value: T) {

        val key: WeakKeyedReference<Any, T> = thisRef?.let {
            map.findWeakReferenceForKey(thisRef) ?: KotlinWeakKeyedReference(thisRef, map)
        } ?: return

        map[key] = value
    }
}

/**
 * Represents a Kotlin mixin'd property using kotlin extension methods by wrapper
 * the JVM weak referenced types with a kotlin coroutine backed garbage collector.
 *
 * Using this with a kotlin extension property getter and server will appear in kotlin source
 * code as a field which has been dynamically added to a class. This of course is not the case since there is no
 * injection happening.
 *
 * The kotlin coroutine GC provides this solution with almost zero overhead. However, this means this implementation
 * is not well suited for long-live instances of the runtime or for storing LARGE amounts of data since all data is stored
 * in memory when using property mixins.
 *
 * Also acts as a DSL for additional features such as data binding and bind aware object transforms.
 *
 * @param T?
 */
class SingleNullPropertyMixin<T, V> : ReadWriteProperty<V, T?> {

    private val map = mutableMapOf<WeakKeyedReference<Any, T>, T>()

    /**
     * Gets the mixin value from the reference map if it is initialized.
     *
     * @param thisRef Any
     * @param property KProperty<*>
     * @return T
     */
    override fun getValue(thisRef: V, property: KProperty<*>): T? = thisRef?.let {
        map[map.findWeakReferenceForKey(thisRef)]
    } ?: throw UninitializedPropertyAccessException("Unable to get the value of the mixin property as it has not been initialized.")

    /**
     * Sets and/or stores a value in the reference map  for a mixin property.
     *
     * @param thisRef Any
     * @param property KProperty<*>
     * @param value T
     */
    override fun setValue(thisRef: V, property: KProperty<*>, value: T?) {

        val key: WeakKeyedReference<Any, T> = thisRef?.let {
            map.findWeakReferenceForKey(thisRef) ?: KotlinWeakKeyedReference(thisRef, map)
        } ?: return

        map[key] = value!!
    }
}