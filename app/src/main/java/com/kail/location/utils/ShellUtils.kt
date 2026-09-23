package com.kail.location.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object ShellUtils {
    private const val TAG = "ShellUtils"
    private val ioExecutor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "KailShellIO").apply { isDaemon = true }
    }

    fun hasRoot(): Boolean {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            val stdout = readTextAsync(process.inputStream)
            val stderr = readTextAsync(process.errorStream)
            process.outputStream.write("id\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val finished = process.waitFor(5_000L, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                KailLog.w(null, TAG, "hasRoot timeout")
                return false
            }
            val output = stdout.awaitText()
            val error = stderr.awaitText()
            if (error.isNotBlank()) {
                KailLog.w(null, TAG, "hasRoot stderr: ${error.trim()}")
            }
            val rooted = output.contains("uid=0") || process.exitValue() == 0
            KailLog.d(null, TAG, "hasRoot=$rooted")
            return rooted
        } catch (e: Exception) {
            KailLog.w(null, TAG, "hasRoot: su unavailable: ${e.message}")
            return false
        } finally {
            process?.destroy()
        }
    }

    fun executeCommand(command: String, timeoutMs: Long = 120_000L): String {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            val stdout = readTextAsync(process.inputStream)
            val stderr = readTextAsync(process.errorStream)
            process.outputStream.write("$command\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val finished = if (timeoutMs > 0) {
                process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            } else {
                process.waitFor()
                true
            }
            if (!finished) {
                process.destroyForcibly()
                process.waitFor(1_000L, TimeUnit.MILLISECONDS)
                KailLog.w(null, TAG, "executeCommand timeout ${timeoutMs}ms [`$command`]")
                return ""
            }
            val stdoutText = stdout.awaitText()
            val stderrText = stderr.awaitText()
            if (stderrText.isNotBlank()) {
                KailLog.w(null, TAG, "executeCommand stderr [`$command`]: ${stderrText.trim()}")
            } else {
                KailLog.d(null, TAG, "executeCommand [`$command`] ok (${stdoutText.length} bytes out)")
            }
            return if (stdoutText.isNotEmpty()) stdoutText else stderrText
        } catch (e: Exception) {
            KailLog.e(null, TAG, "executeCommand failed [`$command`]", e)
            return ""
        } finally {
            process?.destroy()
        }
    }

    fun executeCommandToBytes(command: String, timeoutMs: Long = 120_000L): ByteArray {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            val stdout = readBytesAsync(process.inputStream)
            val stderr = readBytesAsync(process.errorStream)
            process.outputStream.write("$command\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val finished = if (timeoutMs > 0) {
                process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            } else {
                process.waitFor()
                true
            }
            if (!finished) {
                process.destroyForcibly()
                process.waitFor(1_000L, TimeUnit.MILLISECONDS)
                KailLog.w(null, TAG, "executeCommandToBytes timeout ${timeoutMs}ms [`$command`]")
                return ByteArray(0)
            }
            val stdoutBytes = stdout.awaitBytes()
            val stderrBytes = stderr.awaitBytes()
            if (stdoutBytes.isEmpty() && stderrBytes.isNotEmpty()) {
                KailLog.w(null, TAG, "executeCommandToBytes stderr [`$command`]: ${String(stderrBytes).trim()}")
            }
            return if (stdoutBytes.isNotEmpty()) stdoutBytes else stderrBytes
        } catch (e: Exception) {
            KailLog.e(null, TAG, "executeCommandToBytes failed [`$command`]", e)
            return ByteArray(0)
        } finally {
            process?.destroy()
        }
    }

    private fun readTextAsync(input: InputStream): Future<String> {
        return ioExecutor.submit(Callable {
            BufferedReader(InputStreamReader(input)).use { it.readText() }
        })
    }

    private fun readBytesAsync(input: InputStream): Future<ByteArray> {
        return ioExecutor.submit(Callable {
            input.use { it.readBytes() }
        })
    }

    private fun Future<String>.awaitText(): String {
        return runCatching { get(1_000L, TimeUnit.MILLISECONDS) }
            .getOrElse {
                cancel(true)
                ""
            }
    }

    private fun Future<ByteArray>.awaitBytes(): ByteArray {
        return runCatching { get(1_000L, TimeUnit.MILLISECONDS) }
            .getOrElse {
                cancel(true)
                ByteArray(0)
            }
    }
}
