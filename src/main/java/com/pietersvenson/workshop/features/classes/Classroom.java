/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.pietersvenson.workshop.features.classes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Classroom {

  @Getter @NonNull private final String id;
  @Getter @Setter @NonNull private String name; //TODO implement
  @Getter @Setter @NonNull private Curriculum curriculum = Curriculum.NONE;
  @Getter @Setter @NonNull private Schedule schedule = Schedule.empty();
  @Getter @NonNull private final List<Participant> participants = Lists.newLinkedList();

  public Classroom(@Nonnull String id) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(id);
  }

  public boolean addParticipant(@Nonnull Participant participant) {
    return participants.add(Objects.requireNonNull(participant));
  }

  public boolean addParticipants(@Nonnull Collection<Participant> participant) {
    return participants.addAll(Objects.requireNonNull(participant));
  }

  public boolean inClass(@Nonnull UUID playerUuid) {
    return participants.stream().anyMatch(participant ->
        Objects.requireNonNull(playerUuid).equals(participant.getPlayerUuid()));
  }

  public boolean inSession() {
    return schedule.includes(Instant.now());
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Classroom)) {
      return false;
    }
    return id.equals(((Classroom) obj).id);
  }

  @Override
  public String toString() {
    return id;
  }

  public Map.Entry<String, Map<String, Object>> serialize() {
    Map.Entry<String, Map<String, Object>> out = Maps.immutableEntry(id, Maps.newTreeMap());
    out.getValue().put("name", curriculum.name());
    out.getValue().put("curriculum", curriculum.name());
    out.getValue().put("schedule", schedule.serialize());
    out.getValue().put("participants", participants.stream().map(Participant::serialize).collect(Collectors.toList()));
    return out;
  }

  public static Classroom deserialize(String id, Map<String, Object> data) {
    Classroom classroom = new Classroom(id);
    classroom.setName(Optional.ofNullable(data.get("name"))
        .filter(name -> name instanceof String)
        .map(name -> ((String) name))
        .orElse(id));
    classroom.setCurriculum(Optional.ofNullable(data.get("curriculum"))
        .filter(curr -> curr instanceof String)
        .map(curr -> Curriculum.valueOf((String) curr))
        .orElse(Curriculum.NONE));
    classroom.setSchedule(Schedule.deserialize((List<Object>) data.get("schedule")));
    classroom.addParticipants(((List<Object>) data.get("participants"))
            .stream()
            .map(o -> Participant.deserialize((Map<String, String>) o))
            .collect(Collectors.toList()));
    return classroom;
  }

}
