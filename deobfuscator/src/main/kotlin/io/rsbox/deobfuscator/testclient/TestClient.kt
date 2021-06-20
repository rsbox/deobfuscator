package io.rsbox.deobfuscator.testclient

import org.tinylog.kotlin.Logger
import java.applet.Applet
import java.applet.AppletContext
import java.applet.AppletStub
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import javax.swing.JFrame

class TestClient(val gamepack: File) {

    private val params = hashMapOf<String, String>()
    private lateinit var frame: JFrame

    fun start() {
        this.fetchJavConfig()

        /*
         * Create applet
         */
        val classloader = URLClassLoader(arrayOf(gamepack.toURI().toURL()))
        val main = params["initial_class"]!!.replace(".class", "")
        val applet = classloader.loadClass(main).getDeclaredConstructor().newInstance() as Applet
        applet.background = Color.BLACK
        applet.layout = null
        applet.size = Dimension(params["applet_minwidth"]!!.toInt(), params["applet_minheight"]!!.toInt())
        applet.preferredSize = applet.size
        applet.minimumSize = applet.preferredSize
        applet.isVisible = true
        applet.setStub(applet.createAppletStub())
        applet.init()

        /*
         * Create swing jframe
         */
        frame = JFrame("Test Client")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = GridLayout(1, 0)
        frame.setLocationRelativeTo(null)
        frame.add(applet)
        frame.pack()
        frame.isVisible = true

        Logger.info("OSRS test client launched from jar file: '${gamepack.path}'.")
    }

    private fun fetchJavConfig() {
        val url = URL(JAGEX_URL + "jav_config.ws")
        val lines = url.readText(Charsets.UTF_8).split("\n")
        lines.forEach {
            var line = it
            if(line.startsWith("param=")) {
                line = line.substring(6)
            }
            val idx = line.indexOf("=")
            if(idx >= 0) {
                params[line.substring(0, idx)] = line.substring(idx + 1)
            }
        }
    }

    private fun Applet.createAppletStub() = object : AppletStub {
        override fun getParameter(name: String): String? {
            return params[name]
        }

        override fun getDocumentBase(): URL {
            return URL(params["codebase"]!!)
        }

        override fun getCodeBase(): URL {
            return URL(params["codebase"]!!)
        }

        override fun appletResize(width: Int, height: Int) {
            this@createAppletStub.size = Dimension(width, height)
        }

        override fun getAppletContext(): AppletContext? {
            return null
        }

        override fun isActive(): Boolean {
            return true
        }
    }

    companion object {
        private const val JAGEX_URL = "http://oldschool1.runescape.com/"
    }
}