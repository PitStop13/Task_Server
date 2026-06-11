package com.taskserver.app.data.ssh

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class SshManagerTest {

    private val sshManager = SshManager()

    // Helper to call private methods via reflection for testing
    private fun invokePrivateMethod(name: String, vararg args: Any?): Any? {
        val method = sshManager.javaClass.getDeclaredMethod(name, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
        method.isAccessible = true
        return method.invoke(sshManager, *args)
    }

    // Special helper for transformCommandForSudo as it has default parameters and complex signature
    private fun transformCommandForSudo(command: String, sudoPassword: String?, requirePassword: Boolean = true): String {
        val method = sshManager.javaClass.getDeclaredMethods().find { it.name == "transformCommandForSudo" }!!
        method.isAccessible = true
        return method.invoke(null, command, sudoPassword, requirePassword) as String
    }

    @Test
    fun testShellQuote() {
        val method = sshManager.javaClass.getDeclaredMethod("shellQuote", String::class.java)
        method.isAccessible = true
        
        assertEquals("'simple'", method.invoke(null, "simple"))
        assertEquals("'it'\\''s complex'", method.invoke(null, "it's complex"))
        assertEquals("'\"quoted\"'", method.invoke(null, "\"quoted\""))
    }

    @Test
    fun testTransformCommandForSudo() {
        // No sudo prefix
        assertEquals("ls -la", transformCommandForSudo("ls -la", "pass"))
        
        // Simple sudo
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' bash -lc 'apt update'", transformCommandForSudo("sudo apt update", "pass"))
        
        // Sudo with flags
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' -u root ls", transformCommandForSudo("sudo -u root ls", "pass"))
        
        // Sudo with single quotes in command
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' bash -lc 'echo '\\''hello'\\'''", transformCommandForSudo("sudo echo 'hello'", "pass"))
    }
}
