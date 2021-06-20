package io.rsbox.deobfuscator.asm

import io.rsbox.deobfuscator.asm.util.mixin
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

var MethodNode.owner: ClassNode by mixin()
val MethodNode.pool: ClassPool get() = this.owner.pool

internal fun MethodNode.init(owner: ClassNode) {
    this.owner = owner
}

val MethodNode.identifier: String get() = "${this.owner.identifier}.${this.name}${this.desc}"
val MethodNode.type: Type get() = Type.getMethodType(this.desc)
val MethodNode.returnType: Type get() = this.type.returnType
val MethodNode.argTypes: List<Type> get() = this.type.argumentTypes.toList()
val MethodNode.isConstructor get() = (this.name == "<init>")
val MethodNode.isInitializer get() = (this.name == "<clinit>")
