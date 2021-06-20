package io.rsbox.deobfuscator.transformer.controlflow

import org.objectweb.asm.tree.AbstractInsnNode

class Block {

    var startIndex: Int = 0

    var endIndex: Int = 0

    val instructions = mutableListOf<AbstractInsnNode>()

    var prev: Block? = null

    var next: Block? = null

    val branches = mutableListOf<Block>()

    val head: Block get() {
        var block = this
        var last: Block? = prev
        while(last != null) {
            block = last
            last = block.prev
        }

        return block
    }
}