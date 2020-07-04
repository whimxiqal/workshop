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
import com.google.common.collect.Sets;
import com.pietersvenson.workshop.state.Stateful;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClassroomManager implements Stateful {

  /**
   * The draft of the 'schedule' this operator is editing.
   * This is keyed by a String because the operator might be
   * any sort of {@link org.bukkit.command.CommandSender}, not just a
   * {@link org.bukkit.entity.HumanEntity}
   */
  private Map<String, Classroom> classrooms = Maps.newHashMap();
  private Map<String, Classroom.Builder> drafts = Maps.newHashMap();

  public ClassroomManager() {
//    Classroom classroom = Classroom.builder()
//        .setId("test")
//        .setCurriculum(Curriculum.ARCHITECTURE)
//        .setSchedule(Schedule.repeating(
//            new Appointment(Instant.now(), Instant.now().plus(Duration.of(1, ChronoUnit.HOURS))),
//            Duration.of(1, ChronoUnit.DAYS),
//            5))
//        .addParticipant(new Participant("Pieter", "Svenson", UUID.randomUUID()))
//        .build();
//    classrooms.put(classroom.getId(), classroom);
  }

  /**
   * Add a classroom to the global list.
   *
   * @param classroom the classroom
   * @return the preexisting classroom with the given id, or null of none exists
   */
  public Classroom addClassroom(@Nonnull Classroom classroom) {
    return classrooms.put(classroom.getId(), classroom);
  }

  /**
   * Remove a classroom.
   *
   * @param string the id of the classroom
   * @return the classroom which was removed, or null if none was removed
   */
  public Classroom removeClassroom(@Nonnull String string) {
    return classrooms.remove(string);
  }

  public boolean anyRegistered() {
    return !classrooms.isEmpty();
  }

  @Nonnull
  public List<Classroom> getInSession() {
    return classrooms.values().stream().filter(Classroom::inSession).collect(Collectors.toList());
  }

  public boolean hasDraft(String name) {
    return drafts.containsKey(name);
  }

  public Classroom.Builder startDraft(@Nonnull String name) {
    return drafts.put(name, Classroom.builder());
  }

  public Classroom.Builder getDraft(String name) {
    return drafts.get(name);
  }

  public boolean repeat(String name,
                        @Nonnull Duration duration,
                        int count) {
    Classroom.Builder builder = Objects.requireNonNull(drafts.get(name));
    Schedule schedule = builder.getSchedule();
    if (schedule == null || !schedule.isContinuous()) {
      return false;
    }
    builder.setSchedule(schedule.repeat(duration, count));
    return true;
  }

  public boolean repeat(String name,
                        @Nonnull Duration duration,
                        int count,
                        int indexToRepeat) {
    Classroom.Builder builder = Objects.requireNonNull(drafts.get(name));
    Schedule schedule = builder.getSchedule();
    if (schedule == null) {
      return false;
    }
    if (indexToRepeat < 0) {
      return false;
    }
    if (schedule.isContinuous()) {
      if (indexToRepeat == 1) {
        return repeat(name, duration, count);
      } else {
        return false;
      }
    }
    builder.setSchedule(schedule.repeatComponent(duration, count, indexToRepeat));
    return true;
  }

  public int count() {
    return classrooms.size();
  }

  @Nonnull
  @Override
  public String getFileName() {
    return "classrooms.yml";
  }

  @Nonnull
  @Override
  public String dumpState() {
    Map<String, Map<String, Object>> serialized = Maps.newTreeMap();
    classrooms.values().forEach(classroom -> serialized.put(classroom.serialize().getKey(), classroom.serialize().getValue()));
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setIndent(2);
    dumperOptions.setPrettyFlow(true);
    return new Yaml(dumperOptions).dump(serialized);
  }

  @Override
  public void loadState(String state) throws Exception {
    Map<String, Map<String, Object>> toAdd = Maps.newTreeMap();
    try {
      toAdd.putAll(new Yaml().<Map<String, Map<String, Object>>>load(state));
    } catch (Exception e) {
      throw new YAMLException(e);
    }
    classrooms.clear();
    toAdd.forEach((s, map) -> {
      Classroom deserialized = Classroom.deserialize(s, map);
      classrooms.put(deserialized.getId(), deserialized);
    });
  }
}
