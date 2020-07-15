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

package com.pietersvenson.workshop.features;

import com.pietersvenson.workshop.Workshop;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;

public abstract class FeatureManager {

  protected FeatureManager() {
    registerListeners();
  }

  private void registerListeners() {
    getListeners().forEach(listener -> Arrays.stream(listener.getClass().getMethods())
        .filter(method -> method.isAnnotationPresent(FeatureEventHandler.class))
        .filter(method -> method.getParameters().length == 1)
        .forEach(method -> {
          Parameter parameter = method.getParameters()[0];
          if (!Event.class.isAssignableFrom(parameter.getType())) {
            return;
          }
          Bukkit.getPluginManager().registerEvent((Class<? extends Event>) parameter.getType(),
              listener,
              EventPriority.NORMAL,
              (li, event) -> {
                try {
                  if (listener.enabled()) {
                    method.invoke(li, event);
                  }
                } catch (IllegalAccessException | InvocationTargetException e) {
                  e.printStackTrace();
                }
              }, Workshop.getInstance());
        }));
  }

  @Nonnull
  protected abstract Collection<FeatureListener> getListeners();

}
