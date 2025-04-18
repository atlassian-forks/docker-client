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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Date;
import jakarta.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Config {

  @JsonProperty("ID")
  public abstract String id();

  @JsonProperty("Version")
  public abstract Version version();

  @JsonProperty("CreatedAt")
  public abstract Date createdAt();

  @JsonProperty("UpdatedAt")
  public abstract Date updatedAt();

  @JsonProperty("Spec")
  public abstract ConfigSpec configSpec();

  @AutoValue
  public abstract static class Criteria {
    /**
     * Filter by config id.
     */
    @Nullable
    public abstract String configId();

    /**
     * Filter by label.
     */
    @Nullable
    public abstract String label();

    /**
     * Filter by config name.
     */
    @Nullable
    public abstract String name();

    public static Config.Criteria.Builder builder() {
      return new AutoValue_Config_Criteria.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Config.Criteria.Builder configId(String nodeId);

      public abstract Config.Criteria.Builder label(String label);

      public abstract Config.Criteria.Builder name(String nodeName);

      public abstract Config.Criteria build();
    }
  }

  public static Config.Criteria.Builder find() {
    return AutoValue_Config_Criteria.builder();
  }

  @JsonCreator
  static Config create(
      @JsonProperty("ID") final String id,
      @JsonProperty("Version") final Version version,
      @JsonProperty("CreatedAt") final Date createdAt,
      @JsonProperty("UpdatedAt") final Date updatedAt,
      @JsonProperty("Spec") final ConfigSpec secretSpec) {
    return new AutoValue_Config(id, version, createdAt, updatedAt, secretSpec);
  }

}
