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

package com.pietersvenson.workshop.util;

import org.bukkit.ChatColor;

public final class Format {

  private Format() {
  }

  public static final ChatColor THEME = ChatColor.DARK_GRAY;
  public static final ChatColor SUCCESS = ChatColor.GREEN;
  public static final ChatColor INFO = ChatColor.WHITE;
  public static final ChatColor WARN = ChatColor.YELLOW;
  public static final ChatColor ERROR = ChatColor.RED;

  public static String prefix() {
    return THEME + "Workshop % ";
  }

  public static String success(String message) {
    return prefix() + SUCCESS + message;
  }

  public static String info(String message) {
    return prefix() + INFO + message;
  }

  public static String warn(String message) {
    return prefix() + WARN + message;
  }

  public static String error(String message) {
    return prefix() + ERROR + message;
  }

}
