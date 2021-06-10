package blue.mild.covid.vaxx.extensions

import ch.qos.logback.classic.spi.ThrowableProxy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


// see https://github.com/mild-blue/covid-vaxx/issues/287
internal class LogbackExtensionsKtTest {
    @Test
    @Disabled
    fun `this fails on stackoverflow`() {
        val e = Exception("foo")
        val e2 = Exception(e)
        e.initCause(e2)
        ThrowableProxy(e)
    }

    @Test
    @Disabled
    fun `this fails on stackoverflow as well`() {
        val e = Exception("This is foo exception!")
        val e2 = Exception(e)
        e.addSuppressed(e2)
        ThrowableProxy(e)
    }
}
