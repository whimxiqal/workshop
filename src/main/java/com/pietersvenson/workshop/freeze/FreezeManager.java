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

package com.pietersvenson.workshop.freeze;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FreezeManager {

  private boolean allFrozen;
  private Set<UUID> frozenPlayers = Sets.newHashSet();

  public void freezeAll() {
    Bukkit.getServer().getOnlinePlayers().forEach(player -> frozenPlayers.add(player.getUniqueId()));
    allFrozen = true;
  }

  public void unfreezeAll() {
    frozenPlayers.clear();
    allFrozen = false;
  }

  public boolean freeze(Player player) {
    return frozenPlayers.add(player.getUniqueId());
  }

  public boolean unfreeze(Player player) {
    return frozenPlayers.remove(player.getUniqueId());
  }

  public boolean isAllFrozen() {
    return allFrozen;
  }

  public boolean isFrozen(Player player) {
    return frozenPlayers.contains(player.getUniqueId());
  }

}
