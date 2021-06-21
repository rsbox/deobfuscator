package io.rsbox.deobfuscator.transformer.controlflow

import io.rsbox.deobfuscator.Transformer
import io.rsbox.deobfuscator.asm.ClassPool
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.tinylog.kotlin.Logger
import java.util.*
import kotlin.collections.AbstractMap

class ControlFlowNormalizer : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.forEach { cls ->
            cls.methods.filter { it.tryCatchBlocks.isEmpty() }.forEach { method ->
                val insns = method.instructions
                val controlflow = ControlFlowAnalyzer(method)

                val newInsns = InsnList()
                val labelMap = LabelMap()

                if(controlflow.blocks.isNotEmpty()) {
                    val stack = Stack<Block>()
                    val placed = mutableSetOf<Block>()

                    stack.push(controlflow.blocks.first())

                    while(stack.isNotEmpty()) {
                        val block = stack.pop()
                        if(placed.contains(block)) continue
                        placed.add(block)

                        block.branches.forEach { stack.push(it.head) }

                        if(block.next != null) {
                            stack.push(block.next)
                        }

                        for(i in block.startIndex until block.endIndex) {
                            newInsns.add(insns[i].clone(labelMap))
                        }
                    }
                }

                method.instructions = newInsns
                counter += controlflow.blocks.size
            }
        }

        Logger.info("Normalized $counter control-block blocks.")
    }

    private class LabelMap : AbstractMap<LabelNode, LabelNode>() {
        private val map = mutableMapOf<LabelNode, LabelNode>()
        override val entries get() = throw UnsupportedOperationException()
        override fun get(key: LabelNode) = map.getOrPut(key) { LabelNode() }
    }
}