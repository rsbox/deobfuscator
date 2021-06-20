package io.rsbox.deobfuscator

import io.rsbox.deobfuscator.asm.ClassPool

interface Transformer {

    fun transform(pool: ClassPool)

}