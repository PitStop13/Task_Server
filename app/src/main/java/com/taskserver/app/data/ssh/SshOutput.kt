package com.taskserver.app.data.ssh

sealed class SshOutput {
    object Started : SshOutput()
    data class Data(val text: String) : SshOutput()
    data class Error(val message: String) : SshOutput()
    data class Complete(val exitCode: Int) : SshOutput()
    object Cancelled : SshOutput()
}
