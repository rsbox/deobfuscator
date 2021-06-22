package io.rsbox.deobfuscator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.rsbox.deobfuscator.asm.ClassPool
import io.rsbox.deobfuscator.testclient.TestClientCommand
import io.rsbox.deobfuscator.transformer.DeadCodeRemover
import io.rsbox.deobfuscator.transformer.RuntimeExceptionRemover
import io.rsbox.deobfuscator.transformer.ControlFlowNormalizer
import io.rsbox.deobfuscator.transformer.RenameTransformer
import org.tinylog.kotlin.Logger

class Deobfuscator(val pool: ClassPool) {

    private val transformers = mutableListOf<Transformer>()

    /*
     * Register the bytecode transformers this deobfuscator runs.
     */
    init {
        register<RuntimeExceptionRemover>()
        register<DeadCodeRemover>()
        register<ControlFlowNormalizer>()
        register<RenameTransformer>()
    }

    fun run() {
        Logger.info("Running deobfuscator...")

        Logger.info("Found ${transformers.size} bytecode transformers. Preparing to run transformations.")

        transformers.forEach { transformer ->
            val startTime = System.currentTimeMillis()
            Logger.info("Running bytecode transformer: '${transformer::class.simpleName}'.")

            transformer.transform(pool)

            val endTime = System.currentTimeMillis()
            val deltaTimeSeconds = (endTime - startTime) / 1000

            Logger.info("Completed bytecode transformations in $deltaTimeSeconds seconds.")
        }
    }

    private inline fun <reified T : Transformer> register() {
        val klass = T::class.java
        val inst = klass.getDeclaredConstructor().newInstance() as Transformer
        this.transformers.add(inst)
    }
    
    companion object {

        @JvmStatic
        fun main(args: Array<String>) = object : CliktCommand(
            name = "RSBox Deobfuscator",
            help = "Deobfuscates the Jagex OSRS gamepack.",
            printHelpOnEmptyArgs = true,
            invokeWithoutSubcommand = false
        ) {

            override fun run() {
                Logger.info("Initializing...")
            }
        }.subcommands(
            DeobfuscatorCommand(),
            TestClientCommand()
        ).main(args)
    }
}