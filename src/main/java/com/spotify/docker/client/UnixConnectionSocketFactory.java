/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;

import com.google.common.annotations.VisibleForTesting;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

/**
 * Provides a ConnectionSocketFactory for connecting Apache HTTP clients to Unix sockets.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class UnixConnectionSocketFactory implements ConnectionSocketFactory {

  private static final SupplierWithIOException<UnixSocketChannel> UNIX_SOCKET_CHANNEL_SUPPLIER = ()->UnixSocketChannel.open();
  private File socketFile;
  private final SupplierWithIOException<UnixSocketChannel> unixSocketChannelSupplier;

  public UnixConnectionSocketFactory(final URI socketUri) {
    this(socketUri, UNIX_SOCKET_CHANNEL_SUPPLIER);
  }

  @VisibleForTesting
  UnixConnectionSocketFactory(final URI socketUri, SupplierWithIOException<UnixSocketChannel> unixSocketChannelSupplier) {
    super();

    final String filename = socketUri.toString()
            .replaceAll("^unix:///", "unix://localhost/")
            .replaceAll("^unix://localhost", "");

    this.socketFile = new File(filename);
    this.unixSocketChannelSupplier = unixSocketChannelSupplier;
  }

  public static URI sanitizeUri(final URI uri) {
    if (uri.getScheme().equals("unix")) {
      return URI.create("unix://localhost:80");
    } else {
      return uri;
    }
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    UnixSocketChannel unixSocketChannel = unixSocketChannelSupplier.get();
    return new UnixSocket(unixSocketChannel) {
      @Override
      public void connect(SocketAddress addr) throws IOException {
        try {
          getChannel().connect(new UnixSocketAddress(socketFile));
        } catch (SocketTimeoutException e) {
          throw new ConnectTimeoutException("SocketTimeoutException during channel connect operation: " + e.getMessage(), null);
        }
      }

      @Override
      public void connect(SocketAddress addr, int timeout) throws IOException {
        this.setSoTimeout(timeout);
        try {
          getChannel().connect(new UnixSocketAddress(socketFile));
        } catch (SocketTimeoutException e) {
          throw new ConnectTimeoutException("SocketTimeoutException during channel connect operation: " + e.getMessage(), null);
        }
      }
    };
  }

  @Override
  public Socket connectSocket(final TimeValue connectTimeout,
                              final Socket socket,
                              final HttpHost host,
                              final InetSocketAddress remoteAddress,
                              final InetSocketAddress localAddress,
                              final HttpContext context) throws IOException {
    if (!(socket instanceof UnixSocket)) {
      throw new AssertionError("Unexpected socket: " + socket);
    }

    socket.setSoTimeout(connectTimeout.toMillisecondsIntBound());
    try {
      socket.getChannel().connect(new UnixSocketAddress(socketFile));
    } catch (SocketTimeoutException e) {
      throw new ConnectTimeoutException("SocketTimeoutException during channel connect operation: " + e.getMessage(), host);
    }
    return socket;
  }

  @VisibleForTesting
  interface SupplierWithIOException<T> {
    T get() throws IOException;
  }
}
