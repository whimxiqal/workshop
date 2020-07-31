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

import com.google.common.collect.Lists;
import org.bukkit.Material;

import java.util.List;
import java.util.Random;

public final class Randomer {

  private Randomer() {
  }

  public enum WordType {
    ERROR(Lists.newArrayList(
        "Boink",
        "Nope",
        "Nah",
        "Oops",
        "Bonk",
        "Doh",
        "Rats",
        "Fooey"
    ));

    private List<String> list;

    WordType(List<String> list) {
      this.list = list;
    }
  }

  public static String word(WordType type) {
    return chooseRandom(type.list);
  }

  public static Material randomConcrete() {
    Material[] concretes = {
        Material.BLACK_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.LIME_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.PINK_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.RED_CONCRETE,
        Material.WHITE_CONCRETE,
        Material.YELLOW_CONCRETE
    };
    return concretes[new Random().nextInt(16)];
  }

  private static String chooseRandom(List<String> list) {
    return list.get(new Random().nextInt(list.size()));
  }

}
