package io.rsbox.deobfuscator.asm.util

interface WeakKeyedReference<K, V> : WeakReference<K> {

    val map: MutableMap<WeakKeyedReference<K, V>, V>

}