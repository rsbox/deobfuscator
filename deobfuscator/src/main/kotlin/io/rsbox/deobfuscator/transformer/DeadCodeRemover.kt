package io.rsbox.deobfuscator.transformer

import io.rsbox.deobfuscator.Transformer
import io.rsbox.deobfuscator.asm.ClassPool
import io.rsbox.deobfuscator.asm.owner
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.tinylog.kotlin.Logger

class DeadCodeRemover : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.forEach { cls ->
            cls.methods.forEach { method ->
                val frames = Analyzer(BasicInterpreter()).analyze(method.owner.name, method)
                val insns = method.instructions.toArray()
                for(i in frames.indices) {
                    if(frames[i] == null) {
                        method.instructions.remove(insns[i])
                        counter++
                    }
                }
            }
        }

        Logger.info("Removed $counter dead bytecode instructions.")
    }
}