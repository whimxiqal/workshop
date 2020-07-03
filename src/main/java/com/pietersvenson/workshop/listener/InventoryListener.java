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

package com.pietersvenson.workshop.listener;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.permission.Permissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {

  @EventHandler
  public void onInventoryCreative(InventoryCreativeEvent inventoryCreativeEvent) {
    if (!inventoryCreativeEvent.getWhoClicked().hasPermission(Permissions.STAFF)) {
      Workshop.getInstance()
          .getState()
          .getNoitemManager()
          .scheduledClean(inventoryCreativeEvent.getWhoClicked().getInventory());
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
    if (!inventoryClickEvent.getWhoClicked().hasPermission(Permissions.STAFF)) {
      Workshop.getInstance()
          .getState()
          .getNoitemManager()
          .scheduledClean(inventoryClickEvent.getWhoClicked().getInventory());
    }
  }

  @EventHandler
  public void onInventory(PlayerDropItemEvent dropItemEvent) {
    if (!dropItemEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      if (Workshop.getInstance().getState().getNoitemManager().isBanned(dropItemEvent.getItemDrop().getItemStack().getType())) {
        dropItemEvent.getItemDrop().remove();
      }
    }
  }

}
