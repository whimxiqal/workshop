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

package com.pietersvenson.workshop.features.banitem;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureEventHandler;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.permission.Permissions;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;

public class BanitemListener extends FeatureListener {

  protected BanitemListener() {
    super(Settings.ENABLE_BANITEM);
  }

  @FeatureEventHandler
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    Workshop.getInstance().getState().getBanitemManager().scheduledClean(playerJoinEvent.getPlayer().getInventory());
  }

  @FeatureEventHandler
  public void onInventoryCreative(InventoryCreativeEvent inventoryCreativeEvent) {
    if (!inventoryCreativeEvent.getWhoClicked().hasPermission(Permissions.STAFF)) {
      Workshop.getInstance()
          .getState()
          .getBanitemManager()
          .scheduledClean(inventoryCreativeEvent.getWhoClicked().getInventory());
    }
  }

  @FeatureEventHandler
  public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
    if (!inventoryClickEvent.getWhoClicked().hasPermission(Permissions.STAFF)) {
      Workshop.getInstance()
          .getState()
          .getBanitemManager()
          .scheduledClean(inventoryClickEvent.getWhoClicked().getInventory());
    }
  }

  @FeatureEventHandler
  public void onInventoryDrop(PlayerDropItemEvent dropItemEvent) {
    if (!dropItemEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      if (Workshop.getInstance().getState().getBanitemManager().isBanned(dropItemEvent.getItemDrop().getItemStack().getType())) {
        dropItemEvent.getItemDrop().remove();
      }
    }
  }

  @FeatureEventHandler
  public void onInventoryPickup(EntityPickupItemEvent pickupItemEvent) {
    if ((pickupItemEvent.getEntity() instanceof InventoryHolder)
        && !pickupItemEvent.getEntity().hasPermission(Permissions.STAFF)
        && Workshop.getInstance().getState().getBanitemManager().isBanned(pickupItemEvent.getItem().getItemStack().getType())) {
      Workshop.getInstance()
          .getState()
          .getBanitemManager()
          .scheduledClean(((InventoryHolder) pickupItemEvent.getEntity()).getInventory());
    }
  }

}
