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

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureEventHandler;
import com.pietersvenson.workshop.features.FeatureListener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MenuListener extends FeatureListener {

  protected MenuListener() {
    super(Settings.ENABLE_EASY_MENU);
  }

  @FeatureEventHandler
  public void onRightClick(PlayerInteractEvent interactEvent) {
    MenuManager menuManager = Workshop.getInstance().getState().getMenuManager();
    if (interactEvent.getAction() == Action.LEFT_CLICK_AIR
        || interactEvent.getAction() == Action.LEFT_CLICK_BLOCK
        || interactEvent.getHand() != EquipmentSlot.HAND) {
      return;
    }
    boolean clickedBlock = interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK
        && interactEvent.getItem() == null
        && interactEvent.getClickedBlock() != null
        && !interactEvent.getClickedBlock().getType().isInteractable();
    if (clickedBlock) {
      menuManager.rightClicked(interactEvent.getPlayer());
      if (menuManager.isActivated(interactEvent.getPlayer())) {
        menuManager.startUsingMenu(InventoryMenu.easyMenu(interactEvent.getPlayer()), interactEvent.getPlayer());
      }
    }
  }
}
