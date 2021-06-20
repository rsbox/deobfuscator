package io.rsbox.deobfuscator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import io.rsbox.deobfuscator.asm.ClassPool
import org.tinylog.kotlin.Logger

class DeobfuscatorCommand : CliktCommand(
    name = "deobfuscate",
    help = "Deobfuscates the Jagex OSRS gamepack.",
    printHelpOnEmptyArgs = true,
    invokeWithoutSubcommand = true
) {

    private val inputJar by option("-i", "--input-jar", help = "Path to the input jar file.").file(mustExist = true, canBeDir = false).required()
    private val outputJar by option("-o", "--output-jar", help = "Path to output the deobfuscated classes to.").file(canBeDir = false).required()

    override fun run() {
        val pool = ClassPool.fromJar(inputJar)
        val deobfuscator = Deobfuscator(pool)

        deobfuscator.run()

        Logger.info("Saving transformed classes to output jar file.")
        pool.saveJar(outputJar)
    }
}