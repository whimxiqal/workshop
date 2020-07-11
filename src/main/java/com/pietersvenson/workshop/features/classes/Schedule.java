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
import com.pietersvenson.workshop.Workshop;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Schedule {

  public enum Status {
    PRE,
    DURING,
    POST,
    EMPTY
  }

  private LinkedList<Appointment> appointments = Lists.newLinkedList();

  public static Schedule empty() {
    return new Schedule();
  }

  public static Schedule single(@Nonnull Appointment appointment) {
    Schedule out = new Schedule();
    try {
      out.add(Objects.requireNonNull(appointment));
    } catch (OverlappingAppointmentException e) {
      // This will never happen
    }
    return out;
  }

  public static Schedule repeating(@Nonnull Appointment first,
                                   @Nonnull Duration period,
                                   int count) throws OverlappingAppointmentException {
    Schedule out = new Schedule();
    out.addRepeating(
        Objects.requireNonNull(first),
        Objects.requireNonNull(period),
        count);
    return out;
  }

  public static Schedule combine(@Nonnull Collection<Schedule> schedules) throws OverlappingAppointmentException {
    Schedule out = new Schedule();
    for (Schedule input : schedules) {
      for (Appointment appointment : input.appointments) {
        out.add(appointment);
      }
    }
    return out;
  }

  /**
   * Disabled constructor so one must use the static methods.
   */
  private Schedule() {
  }

  public void add(@Nonnull Appointment appointment) throws OverlappingAppointmentException {
    for (Appointment cur : appointments) {
      if (appointment.overlaps(cur)) {
        throw new OverlappingAppointmentException("Appointments can't overlap with each other");
      }
    }
    this.appointments.add(appointment);
    Collections.sort(appointments);
  }

  public void add(@Nonnull Schedule schedule) throws OverlappingAppointmentException {
    for (Appointment appointment : schedule.appointments) {
      add(appointment);
    }
  }

  public void addRepeating(@Nonnull Appointment first,
                           @Nonnull Duration period,
                           int count) throws IllegalArgumentException, OverlappingAppointmentException {
    if (count < 1) {
      throw new IllegalArgumentException("The count must be at least 1");
    }
    Appointment cur = first;
    for (int i = 0; i < count; i++) {
      add(cur);
      cur = cur.shifted(period);
    }
  }

  @Nonnull
  public Status getStatus() {
    if (appointments.isEmpty()) {
      return Status.EMPTY;
    }
    if (appointments.getFirst().getStart().isAfter(Instant.now())) {
      return Status.PRE;
    }
    if (appointments.getLast().getEnd().isAfter(Instant.now())) {
      return Status.DURING;
    }
    return Status.POST;
  }

  public boolean includes(@Nonnull Instant instant) {
    if (appointments.isEmpty()) {
      return false;
    }
    int index = Collections.binarySearch(
        appointments.stream()
            .map(Appointment::getStart)
            .collect(Collectors.toList()),
        instant);
    if (index >= 0) {
      return true;
    }
    if (index == -1 ) {
      return false;
    }
    Workshop.getInstance().getLogger().info("Checking appointment number: " + (Math.abs(index) - 2));
    return appointments.get(Math.abs(index) - 2).includes(instant);
  }

  public boolean overlaps(@Nonnull Schedule other) {
    for (Appointment app1 : appointments) {
      for (Appointment app2 : other.appointments) {
        if (app1.overlaps(app2)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isContinuous() {
    return appointments.size() <= 1;
  }

  public Schedule repeat(@Nonnull Duration duration, int count) throws OverlappingAppointmentException {
    List<Schedule> repeatedAppointments = Lists.newLinkedList();
    for (Appointment appointment : appointments) {
      repeatedAppointments.add(Schedule.repeating(appointment, duration, count));
    }
    return Schedule.combine(repeatedAppointments);
  }

  public Schedule repeatComponent(Duration duration, int count, int indexToRepeat) throws OverlappingAppointmentException {
    if (indexToRepeat < 0 || indexToRepeat >= appointments.size()) {
      throw new IndexOutOfBoundsException("The requested repeated index is out of bounds in this Schedule");
    }
    Schedule out = new Schedule();
    out.appointments.addAll(this.appointments);
    Appointment toRepeat = out.appointments.get(indexToRepeat);
    out.appointments.remove(indexToRepeat);
    out.addRepeating(toRepeat, duration, count);
    return out;
  }

  public void cancel(int appointmentIndex) throws IndexOutOfBoundsException {
    this.appointments.remove(appointmentIndex);
  }

  public LinkedList<Appointment> getAppointments() {
    LinkedList<Appointment> out = Lists.newLinkedList();
    appointments.forEach(app -> out.add(app.copy()));
    return out;
  }

  public List<Object> serialize() {
    return appointments.stream().map(Appointment::serialize).collect(Collectors.toList());
  }

  public static Schedule deserialize(List<Object> data) throws ParseException, OverlappingAppointmentException {
    Schedule out = new Schedule();
    for (Object item : data) {
      out.add(Appointment.deserialize((Map<String, String>) item));
    }
    return out;
  }

  public static class OverlappingAppointmentException extends Exception {

    public OverlappingAppointmentException(String s) {
      super(s);
    }
  }

}
