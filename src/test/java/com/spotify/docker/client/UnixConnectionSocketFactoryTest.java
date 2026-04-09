/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLSocket;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import jnr.unixsocket.UnixSocketOptions;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UnixConnectionSocketFactoryTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private UnixConnectionSocketFactory sut;
  private UnixSocket unixSocket;
  private UnixSocketChannel socketChannel;

  @Before
  public void setup() throws Exception {
    unixSocket = mock(UnixSocket.class);
    socketChannel = mock(UnixSocketChannel.class);
    final UnixSocket unixSocket = mock(UnixSocket.class);
    when(unixSocket.getChannel()).thenReturn(mock(SocketChannel.class));
    sut = new UnixConnectionSocketFactory(new URI("unix://localhost"), ()->socketChannel);
  }

  @Test
  public void testSanitizeUri() {
    final URI unixUri = UnixConnectionSocketFactory.sanitizeUri(URI.create("unix://localhost"));
    assertThat(unixUri, equalTo(URI.create("unix://localhost:80")));

    final URI nonUnixUri = URI.create("http://127.0.0.1");
    final URI uri = UnixConnectionSocketFactory.sanitizeUri(nonUnixUri);
    assertThat(uri, equalTo(nonUnixUri));
  }

  @Test
  public void testConnectSocket() throws Exception {
    when(unixSocket.getChannel()).thenReturn(mock(SocketChannel.class));
    final Socket socket = sut.connectSocket(TimeValue.ofMilliseconds(10), unixSocket, HttpHost.create("http://foo.com"),
        mock(InetSocketAddress.class), mock(InetSocketAddress.class), mock(HttpContext.class));
    verify(unixSocket).setSoTimeout(10);
    assertThat(socket, IsInstanceOf.instanceOf(UnixSocket.class));
    assertThat((UnixSocket) socket, equalTo(unixSocket));
  }

  @Test
  public void testCreateSocketAndConnect() throws Exception {
    final Socket socket = sut.createSocket(null);
    assertThat(socket, IsInstanceOf.instanceOf(UnixSocket.class));

    socket.connect(null, 666);

    verify(socketChannel).setOption(UnixSocketOptions.SO_RCVTIMEO, 666);
    verify(socketChannel).connect((SocketAddress) new UnixSocketAddress(new File("")));
  }

  @Test(expected = AssertionError.class)
  public void testConnectSocketNotUnixSocket() throws Exception {
    sut.connectSocket(TimeValue.ofMilliseconds(10), mock(SSLSocket.class), HttpHost.create("http://foo.com"),
        mock(InetSocketAddress.class), mock(InetSocketAddress.class), mock(HttpContext.class));
  }

}
