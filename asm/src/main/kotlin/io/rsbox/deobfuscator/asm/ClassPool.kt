package io.rsbox.deobfuscator.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class ClassPool private constructor(entries: MutableList<ClassNode> = mutableListOf()) : MutableList<ClassNode> by entries {

    constructor() : this(mutableListOf())

    fun init() {
        this.forEach { cls ->
            cls.init(this)
        }
    }

    fun findClass(name: String): ClassNode? {
        return this.firstOrNull { it.name == name }
    }

    fun replaceClass(old: ClassNode, new: ClassNode) {
        this.remove(old)
        this.add(new)
        new.init(this)
    }

    fun saveJar(file: File) {
        if(file.exists()) {
            file.delete()
        }

        val jos = JarOutputStream(FileOutputStream(file))

        this.forEach { cls ->
            jos.putNextEntry(JarEntry(cls.name + ".class"))
            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
            cls.accept(writer)
            val bytes = writer.toByteArray()
            jos.write(bytes)
            jos.closeEntry()
        }

        jos.close()
    }

    companion object {

        fun fromJar(file: File): ClassPool {
            val pool = ClassPool()

            if(!file.exists()) {
                throw FileNotFoundException("Unable to locate jar file: ${file.path}")
            }

            JarFile(file).use { jar ->
                jar.entries().asSequence()
                    .filter { it.name.endsWith(".class") }
                    .forEach { entry ->
                        val node = ClassNode()
                        val reader = ClassReader(jar.getInputStream(entry))
                        reader.accept(node, ClassReader.SKIP_FRAMES)
                        pool.add(node)
                    }
            }

            pool.init()
            return pool
        }
    }
}