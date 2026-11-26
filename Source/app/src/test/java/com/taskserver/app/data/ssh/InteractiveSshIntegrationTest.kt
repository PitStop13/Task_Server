package com.taskserver.app.data.ssh

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.sshd.common.NamedFactory
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class InteractiveSshIntegrationTest {

    private lateinit var sshServer: SshServer
    private val hostKeyPath = Files.createTempFile("test_hostkey", ".ser")
    private val receivedCommands = CopyOnWriteArrayList<String>()

    @Before
    fun setUp() {
        receivedCommands.clear()
        sshServer = SshServer.setUpDefaultServer().apply {
            host = "127.0.0.1"
            port = 0
            keyPairProvider = SimpleGeneratorHostKeyProvider(hostKeyPath)
            passwordAuthenticator = PasswordAuthenticator { username, password, _ ->
                username == "testuser" && password == "testpass"
            }
            setShellFactory { _: ChannelSession ->
                FakeInteractiveShell(receivedCommands)
            }
        }
        sshServer.start()
    }

    @After
    fun tearDown() {
        try {
            sshServer.stop(true)
        } catch (_: Exception) {}
        Files.deleteIfExists(hostKeyPath)
    }

    @Test
    fun interactiveSession_strictEncryption_executesMultipleSequentialCommands() = runBlocking {
        val manager = SshManager { true }
        val session = manager.startInteractiveSession(
            host = "127.0.0.1",
            port = sshServer.port,
            username = "testuser",
            password = "testpass",
            privateKeyContent = null,
            sudoPassword = "testpass",
            initialCols = 100,
            initialRows = 30
        )

        val outputBuffer = StringBuilder()
        val promptReady = CompletableDeferred<Unit>()
        val allSeen = CompletableDeferred<Unit>()
        val rawSeen = CompletableDeferred<Unit>()

        val collectJob = launch(Dispatchers.IO) {
            session.outputFlow.collect { bytes ->
                val text = String(bytes, Charsets.UTF_8)
                synchronized(outputBuffer) {
                    outputBuffer.append(text)
                    val current = outputBuffer.toString()
                    if (current.contains("SHELL_READY") && !promptReady.isCompleted) {
                        promptReady.complete(Unit)
                    }
                    if (current.contains("OUT:cmd5") && !allSeen.isCompleted) {
                        allSeen.complete(Unit)
                    }
                    if (current.contains("OUT:echo raw") && !rawSeen.isCompleted) {
                        rawSeen.complete(Unit)
                    }
                }
            }
        }

        try {
            withTimeout(10_000) { promptReady.await() }
            assertTrue("Session should remain open after initial banner", session.isOpen())

            val commands = listOf("ls", "pwd", "echo cmd3", "sudo whoami", "echo cmd5")
            for (cmd in commands) {
                assertTrue("Session must still be open before sending '$cmd'", session.isOpen())
                session.sendCommand(cmd)
            }

            withTimeout(10_000) { allSeen.await() }
            assertTrue("Session must stay open after multiple commands", session.isOpen())

            session.resize(120, 40)
            session.sendBytes("echo raw\r".toByteArray(Charsets.UTF_8))
            withTimeout(10_000) { rawSeen.await() }
            assertTrue("Session must stay open after sendBytes", session.isOpen())

            val fullOutput = synchronized(outputBuffer) { outputBuffer.toString() }
            assertTrue(fullOutput.contains("OUT:ls"))
            assertTrue(fullOutput.contains("OUT:pwd"))
            assertTrue(fullOutput.contains("OUT:echo cmd3"))
            assertTrue(fullOutput.contains("OUT:whoami"))
            assertTrue(fullOutput.contains("OUT:cmd5"))
            assertTrue(fullOutput.contains("OUT:echo raw"))
            assertEquals(6, receivedCommands.size)
        } finally {
            session.close()
            collectJob.cancel()
        }

        assertFalse("Session should report closed after close()", session.isOpen())
    }

    @Test
    fun interactiveSession_defaultEncryption_andErrorAfterClose() = runBlocking {
        val manager = SshManager { false }
        val session = manager.startInteractiveSession(
            host = "127.0.0.1",
            port = sshServer.port,
            username = "testuser",
            password = "testpass",
            privateKeyContent = null
        )

        val outputSeen = CompletableDeferred<Unit>()
        val collectJob = launch(Dispatchers.IO) {
            session.outputFlow.collect { bytes ->
                if (String(bytes, Charsets.UTF_8).contains("OUT:second")) {
                    outputSeen.complete(Unit)
                }
            }
        }

        session.sendCommand("first")
        session.sendCommand("second")
        withTimeout(10_000) { outputSeen.await() }
        assertTrue(session.isOpen())

        session.close()
        collectJob.cancel()
        assertFalse(session.isOpen())

        var threwOnClosed = false
        try {
            session.sendCommand("third")
        } catch (_: IllegalStateException) {
            threwOnClosed = true
        }
        assertTrue("Sending on closed session must throw instead of silently succeeding", threwOnClosed)
    }

    private class FakeInteractiveShell(
        private val log: MutableList<String>
    ) : Command {
        private var input: InputStream? = null
        private var output: OutputStream? = null
        private var exitCallback: ExitCallback? = null
        private val running = AtomicBoolean(true)
        private var worker: Thread? = null

        override fun setInputStream(inStream: InputStream) {
            input = inStream
        }

        override fun setOutputStream(out: OutputStream) {
            output = out
        }

        override fun setErrorStream(err: OutputStream) {
            // No-op
        }

        override fun setExitCallback(callback: ExitCallback) {
            exitCallback = callback
        }

        override fun start(channel: ChannelSession, env: Environment) {
            worker = Thread {
                try {
                    val out = output ?: return@Thread
                    out.write("SHELL_READY\r\n$ ".toByteArray(Charsets.UTF_8))
                    out.flush()

                    val reader = BufferedReader(InputStreamReader(input!!, Charsets.UTF_8))
                    val lineBuf = StringBuilder()
                    while (running.get()) {
                        val ch = reader.read()
                        if (ch == -1) break
                        if (ch == '\r'.code || ch == '\n'.code) {
                            val cmd = lineBuf.toString().trim()
                            lineBuf.clear()
                            if (cmd.isNotEmpty()) {
                                log.add(cmd)
                                val label = when {
                                    cmd.contains("echo cmd5") -> "cmd5"
                                    cmd.contains("whoami") -> "whoami"
                                    else -> cmd
                                }
                                out.write("OUT:$label\r\n$ ".toByteArray(Charsets.UTF_8))
                                out.flush()
                            }
                        } else {
                            lineBuf.append(ch.toChar())
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    exitCallback?.onExit(0)
                }
            }.apply {
                isDaemon = true
                start()
            }
        }

        override fun destroy(channel: ChannelSession) {
            running.set(false)
            worker?.interrupt()
        }
    }
}
