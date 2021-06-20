package io.rsbox.deobfuscator.testclient

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

class TestClientCommand : CliktCommand(
    name = "testclient",
    help = "Runs a test OSRS client using a provided gamepack jar file.",
    printHelpOnEmptyArgs = true,
    invokeWithoutSubcommand = true
) {

    private val gamepackJar by option("-i", "--input-jar", help = "Path to the gamepack jar file to run.").file(mustExist = true, canBeDir = false).required()

    override fun run() {
        val client = TestClient(gamepackJar)
        client.start()
    }
}