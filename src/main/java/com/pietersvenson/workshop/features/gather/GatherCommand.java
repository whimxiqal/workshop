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

package com.pietersvenson.workshop.features.gather;

import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.command.common.LambdaCommandNode;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class GatherCommand extends LambdaCommandNode {

  public GatherCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Teleports all non-staff to your location",
        "gather",
        (sender, args) -> {
          if (sender instanceof Player) {
            Player staff = (Player) sender;
            Bukkit.getOnlinePlayers().forEach(player -> {
              if (!player.hasPermission(Permissions.STAFF) && !player.getUniqueId().equals(staff.getUniqueId())) {
                player.teleport(staff.getLocation());
                player.sendMessage(Format.info("You were teleported to " + staff.getName()));
              }
            });
            staff.sendMessage(Format.success("All non-staff were teleported to your location"));
            return true;
          } else {
            sender.sendMessage(Format.error("Only players may execute that command!"));
            return false;
          }
        });
    addChildren(new GatherAllCommand(this));
  }

  static final class GatherAllCommand extends LambdaCommandNode {

    public GatherAllCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Teleports all players to your location",
          "all",
          (sender, args) -> {
            if (sender instanceof Player) {
              Player staff = (Player) sender;
              Bukkit.getOnlinePlayers().forEach(player -> {
                if (!player.getUniqueId().equals(staff.getUniqueId())) {
                  player.teleport(staff.getLocation());
                  player.sendMessage(Format.info("You were teleported to " + staff.getName()));
                }
              });
              staff.sendMessage(Format.success("All players were teleported to your location"));
              return true;
            } else {
              sender.sendMessage(Format.error("Only players may execute that command!"));
              return false;
            }
          });
    }

  }

}
