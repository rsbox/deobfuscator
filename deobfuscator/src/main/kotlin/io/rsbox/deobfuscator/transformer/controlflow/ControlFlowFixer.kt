package io.rsbox.deobfuscator.transformer.controlflow

import io.rsbox.deobfuscator.Transformer
import io.rsbox.deobfuscator.asm.ClassPool
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import java.util.*
import kotlin.collections.AbstractMap

class ControlFlowFixer : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.forEach { cls ->
            cls.methods.forEach { method ->
                if(method.tryCatchBlocks.isEmpty()) {
                    val analyzer = ControlFlowAnalyzer(method)
                    method.instructions = rebuildInsnList(method.instructions, analyzer.blocks)
                }
            }
        }
    }

    private fun rebuildInsnList(originalInsns: InsnList, blocks: List<Block>): InsnList {
        val insns = InsnList()
        if(blocks.isEmpty()) {
            return insns
        }

        val labelMap = LabelMap()
        val stack = Stack<Block>()
        val processed = hashSetOf<Block>()
        stack.push(blocks.first())
        while(stack.isNotEmpty()) {
            val block = stack.pop()
            if(block in processed) continue
            processed.add(block)
            block.branches.forEach { stack.push(it.head) }
            block.next?.let { stack.add(it) }
            for(i in block.startIndex until block.endIndex) {
                insns.add(originalInsns[i].clone(labelMap))
            }
        }

        return insns
    }

    private class LabelMap : AbstractMap<LabelNode, LabelNode>() {
        private val map = hashMapOf<LabelNode, LabelNode>()
        override val entries get() = throw IllegalStateException()
        override fun get(key: LabelNode): LabelNode {
            return map.getOrPut(key) { LabelNode() }
        }
    }
}