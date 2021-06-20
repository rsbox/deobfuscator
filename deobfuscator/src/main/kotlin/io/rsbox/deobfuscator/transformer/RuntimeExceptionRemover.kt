package io.rsbox.deobfuscator.transformer

import io.rsbox.deobfuscator.Transformer
import io.rsbox.deobfuscator.asm.ClassPool
import org.objectweb.asm.Type
import org.tinylog.kotlin.Logger
import java.lang.RuntimeException

class RuntimeExceptionRemover : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.forEach { cls ->
            cls.methods.forEach { method ->
                val initCount = method.tryCatchBlocks.size
                method.tryCatchBlocks.removeIf { it.type == Type.getInternalName(RuntimeException::class.java) }
                counter += initCount - method.tryCatchBlocks.size
            }
        }

        Logger.info("Removed $counter 'RuntimeException' try-catch blocks.")
    }
}