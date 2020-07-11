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
import com.pietersvenson.workshop.config.BooleanSetting;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import com.pietersvenson.workshop.state.Stateful;
import org.bukkit.event.Listener;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassroomManager extends FeatureManager implements Stateful {

  /**
   * The draft of the 'schedule' this operator is editing.
   * This is keyed by a String because the operator might be
   * any sort of {@link org.bukkit.command.CommandSender}, not just a
   * {@link org.bukkit.entity.HumanEntity}
   */
  private Map<String, Classroom> classrooms = Maps.newHashMap();

  /**
   * Add a classroom to the global list.
   *
   * @param classroom the classroom
   * @return the preexisting classroom with the given id, or null of none exists
   */
  public Classroom addClassroom(@Nonnull Classroom classroom) {
    return classrooms.put(classroom.getId(), classroom);
  }

  public Optional<Classroom> getClassroom(@Nonnull String id) {
    return Optional.ofNullable(classrooms.get(id));
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

  public List<String> getClassroomIds() {
    return Lists.newLinkedList(classrooms.keySet());
  }

  public boolean hasAny() {
    return !classrooms.isEmpty();
  }

  @Nonnull
  public List<Classroom> getInSession() {
    return classrooms.values().stream().filter(Classroom::inSession).collect(Collectors.toList());
  }

  public boolean repeat(@Nonnull String name,
                        @Nonnull Duration duration,
                        int count) throws Schedule.OverlappingAppointmentException {
    Classroom classroom = classrooms.get(Objects.requireNonNull(name));
    if (classroom == null) {
      return false;
    }
    Schedule schedule = classroom.getSchedule();
    if (!schedule.isContinuous()) {
      return false;
    }
    classroom.setSchedule(schedule.repeat(duration, count));
    return true;
  }

  public boolean repeat(String name,
                        @Nonnull Duration duration,
                        int count,
                        int indexToRepeat) throws Schedule.OverlappingAppointmentException {
    Classroom classroom = classrooms.get(Objects.requireNonNull(name));
    if (classroom == null) {
      return false;
    }
    Schedule schedule = classroom.getSchedule();
    if (indexToRepeat < 0) {
      return false;
    }
    if (!schedule.isContinuous()) {
      if (indexToRepeat == 1) {
        return repeat(name, duration, count);
      } else {
        return false;
      }
    }
    classroom.setSchedule(schedule.repeatComponent(duration, count, indexToRepeat));
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
    for (Map.Entry<String, Map<String, Object>> entry : toAdd.entrySet()) {
      Classroom deserialized = Classroom.deserialize(entry.getKey(), entry.getValue());
      classrooms.put(deserialized.getId(), deserialized);
    }
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Lists.newArrayList(new ClassroomListener(), new ClassroomNicknameListener());
  }
}
