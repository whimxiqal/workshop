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

package com.pietersvenson.workshop.features.menu;

import com.google.common.collect.Maps;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class MenuManager extends FeatureManager implements Listener {

  private HashMap<UUID, InventoryMenu> usingMenu = Maps.newHashMap();
  private HashMap<UUID, Integer> rightClicks = Maps.newHashMap();

  public MenuManager() {
    Bukkit.getPluginManager().registerEvents(this, Workshop.getInstance());
  }

  public void startUsingMenu(InventoryMenu menu, HumanEntity player) {
    player.closeInventory();
    menu.open(player);
    usingMenu.put(player.getUniqueId(), menu);
  }

  public void stopUsingMenu(HumanEntity player) {
    player.closeInventory();
  }

  public boolean isUsingMenu(HumanEntity player) {
    return usingMenu.containsKey(player.getUniqueId());
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player)) {
      return;
    }
    if (isUsingMenu(e.getWhoClicked())) {
      InventoryMenu menu = usingMenu.get(e.getWhoClicked().getUniqueId());
      menu.getItem(e.getSlot()).ifPresent(item -> {
        e.getWhoClicked().closeInventory();
        item.run();
      });
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    this.usingMenu.remove(e.getPlayer().getUniqueId());
  }

  public void rightClicked(Player player) {
    this.rightClicks.putIfAbsent(player.getUniqueId(), 0);
    this.rightClicks.put(player.getUniqueId(), this.rightClicks.get(player.getUniqueId()) + 1);
    Bukkit.getScheduler().runTaskLater(
        Workshop.getInstance(),
        () -> {
          this.rightClicks.put(player.getUniqueId(), this.rightClicks.get(player.getUniqueId()) - 1);
        },
        Settings.EASY_MENU_TIMEOUT.getValue());
  }

  public boolean isActivated(Player player) {
    return this.rightClicks.get(player.getUniqueId()) != null
        && this.rightClicks.get(player.getUniqueId()) >= Settings.EASY_MENU_CLICK_COUNT.getValue();
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Collections.singleton(new MenuListener());
  }

}
