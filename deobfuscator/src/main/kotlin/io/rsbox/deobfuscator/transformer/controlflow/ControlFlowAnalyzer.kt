package io.rsbox.deobfuscator.transformer.controlflow

import io.rsbox.deobfuscator.asm.owner
import org.objectweb.asm.tree.AbstractInsnNode.JUMP_INSN
import org.objectweb.asm.tree.AbstractInsnNode.LABEL
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue

class ControlFlowAnalyzer(val method: MethodNode) {

    val blocks = mutableListOf<Block>()

    private val analyzer = object : Analyzer<BasicValue>(BasicInterpreter()) {

        override fun init(owner: String, method: MethodNode) {
            val insns = method.instructions
            var currentBlock = Block()
            blocks.add(currentBlock)
            for(i in 0 until insns.size()) {
                val insn = insns[i]
                currentBlock.instructions.add(insn)
                currentBlock.endIndex++
                if(insn.next == null) break
                if(insn.next.type == LABEL ||
                        insn.type == JUMP_INSN ||
                        insn.type == LOOKUPSWITCH ||
                        insn.type == TABLESWITCH) {
                    currentBlock = Block()
                    currentBlock.startIndex = i + 1
                    currentBlock.endIndex = i + 1
                    blocks.add(currentBlock)
                }
            }
        }

        override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
            val currentBlock = findBlock(insnIndex)
            val successorBlock = findBlock(successorIndex)
            if(currentBlock != successorBlock) {
                if(insnIndex + 1 == successorIndex) {
                    currentBlock.next = successorBlock
                    successorBlock.prev = currentBlock
                } else {
                    currentBlock.branches.add(successorBlock)
                }
            }
        }

        private fun findBlock(index: Int): Block {
            return blocks.first { index in it.startIndex until it.endIndex }
        }
    }

    init {
        this.analyzer.analyze(method.owner.name, method)
    }
}