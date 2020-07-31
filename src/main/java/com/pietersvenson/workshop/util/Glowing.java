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

import com.pietersvenson.workshop.Workshop;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class Glowing {

  public static void register(Workshop plugin) {
    try {
      Field f = Enchantment.class.getDeclaredField("acceptingNew");
      f.setAccessible(true);
      f.set(null, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      NamespacedKey key = new NamespacedKey(plugin, "glow");
      Glow glow = new Glow(key);
      Enchantment.registerEnchantment(glow);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static final class Glow extends Enchantment {

    public Glow(NamespacedKey key) {
      super(key);
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public int getMaxLevel() {
      return 0;
    }

    @Override
    public int getStartLevel() {
      return 0;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
      return null;
    }

    @Override
    public boolean isTreasure() {
      return false;
    }

    @Override
    public boolean isCursed() {
      return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
      return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
      return false;
    }
  }

}
