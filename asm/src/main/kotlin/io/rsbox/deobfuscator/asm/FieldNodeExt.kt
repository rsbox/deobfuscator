package io.rsbox.deobfuscator.asm

import io.rsbox.deobfuscator.asm.util.mixin
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode

var FieldNode.owner: ClassNode by mixin()
val FieldNode.pool: ClassPool get() = this.owner.pool

internal fun FieldNode.init(owner: ClassNode) {
    this.owner = owner
}

val FieldNode.type: Type get() = Type.getType(this.desc)
val FieldNode.identifier: String get() = "${this.owner.identifier}.${this.name}"