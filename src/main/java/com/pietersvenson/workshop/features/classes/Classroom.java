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
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Classroom {

  @Getter @NonNull private final String id;
  @Getter @NonNull private final Curriculum curriculum;
  @Getter @NonNull private final Schedule schedule;
  @Getter @NonNull private final List<Participant> participants = Lists.newLinkedList();

  public boolean addParticipant(@Nonnull Participant participant) {
    return participants.add(Objects.requireNonNull(participant));
  }

  public boolean inClass(@Nonnull UUID playerUuid) {
    return participants.stream().anyMatch(participant ->
        Objects.requireNonNull(playerUuid).equals(participant.getPlayerUuid()));
  }

  public boolean inSession() {
    return schedule.includes(Instant.now());
  }

  public static Builder builder() {
    return new Classroom.Builder();
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
    out.getValue().put("curriculum", curriculum.name());
    out.getValue().put("schedule", schedule.serialize());
    out.getValue().put("participants", participants.stream().map(Participant::serialize).collect(Collectors.toList()));
    return out;
  }

  public static Classroom deserialize(String id, Map<String, Object> data) {
    return builder()
        .setId(id)
        .setCurriculum(Curriculum.valueOf((String) data.get("curriculum")))
        .setSchedule(Schedule.deserialize((List<Object>) data.get("schedule")))
        .addParticipants(((List<Object>) data.get("participants"))
            .stream()
            .map(o -> Participant.deserialize((Map<String, String>) o))
            .collect(Collectors.toList()))
        .build();
  }

  public static class Builder {

    @Getter private String id;
    @Getter private Curriculum curriculum;
    @Getter private Schedule schedule;
    @Getter private final List<Participant> participants = Lists.newLinkedList();

    private Builder() {
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setCurriculum(Curriculum curriculum) {
      this.curriculum = curriculum;
      return this;
    }

    public Builder setSchedule(Schedule schedule) {
      this.schedule = schedule;
      return this;
    }

    public Classroom build() {
      if (id == null) {
        throw new IllegalStateException("The id cannot be null");
      }
      if (curriculum == null) {
        throw new IllegalStateException("The curriculum cannot be null");
      }
      if (schedule == null) {
        throw new IllegalStateException("The schedule cannot be null");
      }

      Classroom out = new Classroom(id, curriculum, schedule);
      participants.forEach(out::addParticipant);
      return out;
    }

    public Builder addParticipant(@Nonnull Participant participant) {
      participants.add(Objects.requireNonNull(participant));
      return this;
    }

    public Builder addParticipants(@Nonnull Collection<Participant> participants) {
      this.participants.addAll(participants);
      return this;
    }

  }

}
