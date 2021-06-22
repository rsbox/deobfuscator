package io.rsbox.deobfuscator.transformer

import io.rsbox.deobfuscator.Transformer
import io.rsbox.deobfuscator.asm.*
import io.rsbox.deobfuscator.asm.util.InheritanceGraph
import io.rsbox.deobfuscator.util.isObfuscatedName
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.objectweb.asm.tree.ClassNode
import org.tinylog.kotlin.Logger

class RenameTransformer : Transformer {

    private var classCounter = 0
    private var methodCounter = 0
    private var fieldCounter = 0

    private val mappings = hashMapOf<String, String>()

    override fun transform(pool: ClassPool) {
        this.generateMappings(pool)
        this.applyMappings(pool)

        Logger.info("Renamed $classCounter obfuscated classes.")
        Logger.info("Renamed $methodCounter obfuscated methods.")
        Logger.info("Renamed $fieldCounter obfuscated fields.")
    }

    private fun generateMappings(pool: ClassPool) {
        Logger.info("Generating name mappings...")

        val inheritanceGraph = InheritanceGraph(pool)

        /*
         * Generate class name mappings
         */
        pool.forEach { cls ->
            if(!cls.name.isObfuscatedName()) return@forEach
            mappings[cls.identifier] = "class${++classCounter}"
        }

        /*
         * Generate method name mappings.
         */
        pool.flatMap { it.methods }.forEach { method ->
            if(mappings.containsKey(method.identifier) || !method.name.isObfuscatedName()) return@forEach

            val newName = "method${++methodCounter}"
            mappings[method.identifier] = newName

            /*
             * Visit the inheritors to generate mappings for them too.
             */
            inheritanceGraph.findChildren(method.owner.name).forEach { child ->
                mappings["$child.${method.name}${method.desc}"] = newName
            }
        }

        /*
         * Generate field names
         */
        pool.flatMap { it.fields }.forEach { field ->
            if(mappings.containsKey(field.identifier) || !field.name.isObfuscatedName()) return@forEach

            val newName = "field${++fieldCounter}"
            mappings[field.identifier] = newName

            /*
             * Visit the inheritors to generate mappings for them too.
             */
            inheritanceGraph.findChildren(field.owner.name).forEach { child ->
                mappings["$child.${field.name}"] = newName
            }
        }
    }

    private fun applyMappings(pool: ClassPool) {
        Logger.info("Applying newly generated names...")

        val newNodes = mutableListOf<ClassNode>()
        val remapper = SimpleRemapper(mappings)

        pool.forEach { cls ->
            val newNode = ClassNode()
            val classRemapper = ClassRemapper(newNode, remapper)
            cls.accept(classRemapper)
            newNodes.add(newNode)
        }

        pool.clear()
        pool.addAll(newNodes)
    }
}