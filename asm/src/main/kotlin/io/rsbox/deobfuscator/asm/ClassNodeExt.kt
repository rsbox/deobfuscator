package io.rsbox.deobfuscator.asm

import io.rsbox.deobfuscator.asm.util.mixin
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

var ClassNode.pool: ClassPool by mixin()

internal fun ClassNode.init(pool: ClassPool) {
    this.pool = pool
    this.methods.forEach { it.init(this) }
    this.fields.forEach { it.init(this) }
}

val ClassNode.type: Type get() = Type.getObjectType(this.name)

val ClassNode.identifier: String get() = this.name

fun ClassNode.findMethod(name: String, desc: String): MethodNode? {
    return this.methods.firstOrNull { it.name == name && it.desc == desc }
}

fun ClassNode.findField(name: String, desc: String): FieldNode? {
    return this.fields.firstOrNull { it.name == name && it.desc == desc }
}

