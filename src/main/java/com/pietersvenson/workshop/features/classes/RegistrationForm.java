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

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class RegistrationForm {

  private enum Stage {
    FIRST_NAME("Please type your first name", (data, reg) -> reg.firstName = data),
    LAST_NAME("Please type your last name", (data, reg) -> reg.lastName = data);

    String message;
    BiConsumer<String, RegistrationForm> consumer;

    Stage(String message, BiConsumer<String, RegistrationForm> consumer) {
      this.message = message;
      this.consumer = consumer;
    }
  }

  private int cursor = 0;
  private String firstName;
  private String lastName;

  public String message() {
    return Stage.values()[cursor].message;
  }

  public void input(@Nonnull String data) {
    Stage.values()[cursor++].consumer.accept(Objects.requireNonNull(data), this);
  }

  public boolean isDone() {
    return cursor >= Stage.values().length;
  }

  public Classroom.Participant complete(@Nonnull UUID uuid) {
    return new Classroom.Participant(firstName, lastName, Objects.requireNonNull(uuid));
  }

}
