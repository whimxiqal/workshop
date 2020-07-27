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

package com.pietersvenson.workshop.config;

/**
 * An enumeration of all Settings. No need to register anywhere, that's done dynamically.
 */
public final class Settings {

  private Settings() {
  }

  public static final Setting<Boolean> ENABLE_CLASSES = new Setting<>("features.classes.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_NICKNAME_PARTICIPANTS = new Setting<>("features.classes.nickname-participants", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_FREEZE = new Setting<>("features.freeze.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_NICKNAME_COMMAND = new Setting<>("features.nicknames.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_HOMES = new Setting<>("features.homes.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_SPAWN = new Setting<>("features.spawn.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_NOITEM = new Setting<>("features.noitem.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_TELEPORTING = new Setting<>("features.tprequest.enable", true, Boolean.class);
  public static final Setting<Boolean> ENABLE_TECTONIC = new Setting<>("features.tectonic.enable", true, Boolean.class);
  public static final Setting<Integer> TELEPORTING_TIMEOUT = new Setting<>("features.tprequest.timeout", 30, Integer.class);

}
