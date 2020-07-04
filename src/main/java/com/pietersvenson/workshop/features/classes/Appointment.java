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

import com.google.common.collect.Maps;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class Appointment implements Comparable<Appointment> {

  @Getter private final Instant start;
  @Getter private final Instant end;

  public Appointment(@Nonnull Instant start,
                     @Nonnull Instant end) throws IllegalArgumentException {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("The end date cannot be after the start date!");
    }
    this.start = start;
    this.end = end;
  }

  public boolean includes(@Nonnull Instant date) {
    return date.isAfter(start) && date.isBefore(end);
  }

  public boolean overlaps(@Nonnull Appointment other) {
    return this.includes(other.getStart()) || this.includes(other.getEnd());
  }

  @Nonnull
  public Appointment shifted(@Nonnull Duration duration) {
    return new Appointment(start.plus(duration), end.plus(duration));
  }

  @Override
  public int compareTo(Appointment o) {
    return this.start.compareTo(o.getStart());
  }

  public Schedule repeat(@Nonnull Duration duration, int count) {
    return Schedule.repeating(this, duration, count);
  }

  public Map<String, Long> serialize() {
    Map<String, Long> out = Maps.newTreeMap();
    out.put("start", start.getEpochSecond());
    out.put("end", end.getEpochSecond());
    return out;
  }

  public static Appointment deserialize(Map<String, Integer> data) {
    return new Appointment(Instant.ofEpochSecond(data.get("start")), Instant.ofEpochSecond(data.get("end")));
  }

}
