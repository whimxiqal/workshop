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

package com.pietersvenson.workshop.features.freeze;

import com.google.common.collect.Sets;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class FreezeManager extends FeatureManager {

  private boolean allFrozen;
  private Set<UUID> frozenPlayers = Sets.newHashSet();

  public void freezeAll() {
    Bukkit.getOnlinePlayers().forEach(this::freeze);
    allFrozen = true;
  }

  public void unfreezeAll() {
    Bukkit.getOnlinePlayers().forEach(this::unfreeze);
    frozenPlayers.clear();
    allFrozen = false;
  }

  /**
   * Freeze a player.
   *
   * @param player the player
   * @return true if player was frozen properly
   */
  public boolean freeze(Player player) {
    if (player.hasPermission(Permissions.STAFF)) {
      return false;
    }
    boolean out = frozenPlayers.add(player.getUniqueId());
    if (out) {
      player.sendMessage(Format.error("You have been frozen!"));
    }
    return out;
  }

  /**
   * Unfreeze a player.
   *
   * @param player the player
   * @return true if player was unfrozen properly
   */
  public boolean unfreeze(Player player) {
    boolean out = frozenPlayers.remove(player.getUniqueId());
    if (out) {
      player.sendMessage(Format.success("You have been unfrozen!"));
    }
    return out;
  }

  public boolean isAllFrozen() {
    return allFrozen;
  }

  public boolean isFrozen(Player player) {
    return frozenPlayers.contains(player.getUniqueId());
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Collections.singleton(new FreezeListener());
  }
}
