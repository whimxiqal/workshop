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

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureEventHandler;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class FreezeListener extends FeatureListener {

  protected FreezeListener() {
    super(Settings.ENABLE_FREEZE);
  }

  @FeatureEventHandler
  public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerMoveEvent.getPlayer())) {
      playerMoveEvent.setCancelled(true);
    }
  }

  @FeatureEventHandler
  public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerInteractEvent.getPlayer())) {
      playerInteractEvent.setCancelled(true);
    }
  }

  @FeatureEventHandler
  public void onPlayerChat(AsyncPlayerChatEvent playerChatEvent) {
    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerChatEvent.getPlayer())) {
      playerChatEvent.setCancelled(true);
      playerChatEvent.getPlayer().sendMessage(Format.error("You can't chat when you are frozen!"));
    }
  }

  @FeatureEventHandler
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    Player player = playerJoinEvent.getPlayer();
    List<Classroom> progressing = Workshop.getInstance().getState().getClassroomManager().getInSession();

    if (!player.hasPermission(Permissions.STAFF)) {

      if (Workshop.getInstance().getState().getFreezeManager().isAllFrozen()) {
        Workshop.getInstance().getState().getFreezeManager().freeze(player);
      }

    }
  }

}
