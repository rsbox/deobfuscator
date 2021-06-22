package io.rsbox.deobfuscator.util

fun String.isObfuscatedName(): Boolean {
    return (this.length <= 2) || (this.length == 3 && listOf("aa", "ab", "ac", "ad", "ae").any { this.startsWith(it) })
}