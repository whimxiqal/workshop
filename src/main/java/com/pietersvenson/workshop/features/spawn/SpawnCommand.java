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

package com.pietersvenson.workshop.features.spawn;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public final class SpawnCommand extends CommandNode {

  public SpawnCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.COMMAND_ROOT,
        "Go to the custom set spawnpoint",
        "spawn");
    addChildren(new SpawnSetCommand(this));
    setEnabler(Settings.ENABLE_SPAWN);
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may execute this command!"));
      return false;
    }
    Player player = (Player) sender;

    Optional<Location> spawn = Workshop.getInstance().getState().getSpawnManager().getSpawn();
    if (spawn.isPresent()) {
      player.teleport(spawn.get());
      player.sendMessage(Format.success("Teleported!"));
      return true;
    } else {
      player.sendMessage(Format.error("No spawnpoint has been set yet!"));
      return false;
    }
  }

  static final class SpawnSetCommand extends CommandNode {

    public SpawnSetCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.STAFF, "Set the spawnpoint", "set");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(Format.error("Only players may execute this command!"));
        return false;
      }
      Workshop.getInstance().getState().getSpawnManager().setSpawn(((Player) sender).getLocation());
      sender.sendMessage(Format.success("Spawn set!"));
      return false;
    }
  }

}
