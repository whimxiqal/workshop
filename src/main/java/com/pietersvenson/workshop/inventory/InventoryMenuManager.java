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

package com.pietersvenson.workshop.inventory;

import com.google.common.collect.Maps;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.features.DeafFeatureManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.UUID;

public class InventoryMenuManager extends DeafFeatureManager implements Listener {

  private HashMap<UUID, InventoryMenu> usingMenu = Maps.newHashMap();

  public InventoryMenuManager() {
    Bukkit.getPluginManager().registerEvents(this, Workshop.getInstance());
  }

  public void startUsing(InventoryMenu menu, HumanEntity player) {
    menu.open(player);
    usingMenu.put(player.getUniqueId(), menu);
  }

  public void stopUsing(HumanEntity player) {
    usingMenu.remove(player.getUniqueId());
  }

  public boolean isUsing(HumanEntity player) {
    return usingMenu.containsKey(player.getUniqueId());
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player)) {
      return;
    }
    if (isUsing(e.getWhoClicked())) {
      InventoryMenu menu = usingMenu.get(e.getWhoClicked().getUniqueId());
      menu.getItem(e.getSlot()).ifPresent(item -> {
        e.getWhoClicked().closeInventory();
        item.accept((Player) e.getWhoClicked());
      });
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    if (isUsing(e.getPlayer())) {
      stopUsing(e.getPlayer());
    }
  }

}
