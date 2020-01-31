/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.cactoos.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import org.cactoos.http.io.ReadBytes;
import org.cactoos.text.TextOf;
import org.junit.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.llorllale.cactoos.matchers.IsTrue;
import org.llorllale.cactoos.matchers.TextHasString;
import org.llorllale.cactoos.matchers.Throws;
import org.takes.http.FtRemote;
import org.takes.tk.TkText;

/**
 * Test case for {@link HtAutoClosedResponse}.
 *
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class HtAutoClosedResponseTest {
    @Test
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void closesResponseAndThusSocketWhenEofIsReached() throws Exception {
        new FtRemote(new TkText("Hey")).exec(
            home -> {
                @SuppressWarnings("resource")
                final Socket socket = new Socket(
                    home.getHost(),
                    home.getPort()
                );
                try (InputStream ins = new HtAutoClosedResponse(
                    new HtResponse(
                        new HtWire(() -> socket),
                        new Get(home)
                    )
                ).stream()) {
                    new Assertion<>(
                        "must have a response",
                        new TextOf(new ReadBytes(ins)),
                        new TextHasString("HTTP/1.1 200 OK")
                    ).affirm();
                    new Assertion<>(
                        "must close the response, thus the socket, after EOF",
                        socket.isClosed(),
                        new IsTrue()
                    ).affirm();
                    new Assertion<>(
                        "must behave as closed",
                        ins::available,
                        new Throws<>("Socket closed", SocketException.class)
                    ).affirm();
                    // @checkstyle IllegalCatchCheck (1 line)
                } catch (final Exception ex) {
                    throw new IOException(ex);
                }
            }
        );
    }
}
