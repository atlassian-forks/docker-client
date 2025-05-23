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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

import jakarta.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Placement {

  @Nullable
  @JsonProperty("Constraints")
  public abstract ImmutableList<String> constraints();

  @Nullable
  @JsonProperty("Preferences")
  public abstract ImmutableList<Preference> preferences();



  public static Placement create(final List<String> constraints) {
    return create(constraints, null);
  }

  @JsonCreator
  public static Placement create(@JsonProperty("Constraints") final List<String> constraints,
                                 @JsonProperty("Preferences") final List<Preference> preferences) {
    final ImmutableList<String> constraintsT = constraints == null
                                               ? null : ImmutableList.copyOf(constraints);

    final ImmutableList<Preference> preferencesT = preferences == null
                                              ? null : ImmutableList.copyOf(preferences);

    return new AutoValue_Placement(constraintsT, preferencesT);
  }
}
