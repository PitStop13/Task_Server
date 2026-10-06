package com.taskserver.app.data.ssh

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class SshManagerTest {

    // Helper for pure functions that do not require SshManager instantiation

    // The methods are top-level private in the file
    private val sshManagerKtClass = Class.forName("com.taskserver.app.data.ssh.SshManagerKt")

    private fun transformCommandForSudo(command: String, sudoPassword: String?, requirePassword: Boolean = true): String {
        val method = sshManagerKtClass.getDeclaredMethods().find { it.name == "transformCommandForSudo" }!!
        method.isAccessible = true
        return method.invoke(null, command, sudoPassword, requirePassword) as String
    }

    @Test
    fun testShellQuote() {
        val method = sshManagerKtClass.getDeclaredMethods().find { it.name == "shellQuote" }!!
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
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' sh -c 'apt update'", transformCommandForSudo("sudo apt update", "pass"))
        
        // Sudo with flags
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' -u root ls", transformCommandForSudo("sudo -u root ls", "pass"))
        
        // Sudo with single quotes in command
        assertEquals("printf '%s\\n' 'pass' | sudo -S -p '' sh -c 'echo '\\''hello'\\'''", transformCommandForSudo("sudo echo 'hello'", "pass"))
    }
}
