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

package com.pietersvenson.workshop.command.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Builder
public class Parameter {

  @NonNull
  private ParameterSupplier supplier;
  private Permission permission;
  private Parameter next;

  public ParameterSupplier getSupplier() {
    if (supplier == null) {
      return ParameterSuppliers.NONE;
    }
    return supplier;
  }

  public Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  private Optional<Parameter> getNext() {
    return Optional.ofNullable(next);
  }

  public List<String> getNextEntries(Permissible permissible, String[] toParse) {
    if (getPermission().isPresent() && !permissible.hasPermission(this.permission)) {
      return Lists.newLinkedList();
    }
    if (toParse.length == 0) {
      return supplier.getAllowedEntries();
    }
    if (this.supplier.matches(toParse[0])) {
      return getNext()
          .map(next -> next.getNextEntries(permissible, Arrays.copyOfRange(toParse, 1, toParse.length)))
          .orElse(Lists.newArrayList());
    }
    return Lists.newLinkedList();
  }

  public static Parameter chain(@Nonnull Parameter first, @Nonnull Parameter... others) {
    Preconditions.checkNotNull(first);
    Preconditions.checkNotNull(others);
    List<Parameter> parameters = Lists.newArrayList(first);
    parameters.addAll(Arrays.asList(others));
    for (int i = 0; i < parameters.size() - 1; i++) {
      Parameter cur = parameters.get(i);
      while (cur.getNext().isPresent()) {
        cur = cur.getNext().get();
      }
      cur.next = parameters.get(i + 1);
    }
    return parameters.get(0);
  }

  public Optional<String> getFullUsage(@Nonnull Permissible permissible) {
    if (getPermission().isPresent() && !permissible.hasPermission(getPermission().get())) {
      return Optional.empty();
    }
    StringBuilder builder = new StringBuilder();
    Parameter cur = this;
    builder.append(cur.getSupplier().getUsage());
    while (cur.getNext().isPresent()
        && cur.getNext().get().getPermission().isPresent()
        && permissible.hasPermission(cur.getNext().get().getPermission().get())) {
      cur = cur.next;
      builder.append(" ");
      builder.append(cur.getSupplier().getUsage());
    }
    return Optional.of(builder.toString());
  }

  @Builder
  public static class ParameterSupplier {
    private final Supplier<List<String>> allowedEntries;
    @Getter
    private final String usage;

    public List<String> getAllowedEntries() {
      return Optional.ofNullable(allowedEntries).map(Supplier::get).orElse(Lists.newLinkedList());
    }

    public boolean matches(String input) {
      return allowedEntries.get().stream().anyMatch(allowed -> allowed.equalsIgnoreCase(input));
    }

  }

}
