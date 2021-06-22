package io.rsbox.deobfuscator.asm.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class KotlinWeakKeyedReference<K, V>(
    key: K,
    override val map: MutableMap<WeakKeyedReference<K, V>, V>
) : KotlinWeakReference<K>(key), WeakKeyedReference<K, V>  {

    /*
     * Use a global scoped coroutine to clear the queue of the reference.
     * This fixes an issue where the JVM fails to garbage collect stored references
     * after they are no longer needed.
     */
    init {
        GlobalScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            queue.remove()
            map.remove(this@KotlinWeakKeyedReference)
        }
    }
}