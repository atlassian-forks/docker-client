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

import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;
import com.spotify.docker.client.messages.Event;

import java.io.Closeable;
import java.io.IOException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.io.CloseMode;

public class EventReader implements Closeable {

  private final ObjectMapper objectMapper;
  private final CloseableHttpResponse response;
  private JsonParser parser;

  public EventReader(final CloseableHttpResponse response, final ObjectMapper objectMapper) {
    this.response = response;
    this.objectMapper = objectMapper;
  }

  public Event nextMessage() throws IOException {
    if (this.parser == null) {
      this.parser = objectMapper.createParser(response.getEntity().getContent());
    }

    // If the parser is closed, there's no new event
    if (this.parser.isClosed()) {
      return null;
    }

    // Read tokens until we get a start object
    if (parser.nextToken() == null) {
      return null;
    }

    return parser.readValueAs(Event.class);
  }

  @Override
  public void close() throws IOException {
    response.close(CloseMode.IMMEDIATE);
  }

}
