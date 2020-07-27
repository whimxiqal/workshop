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

package com.pietersvenson.workshop.features.teleport;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandError;
import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportAcceptCommand extends CommandNode {
  public TeleportAcceptCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.COMMAND_ROOT, "Accept incoming teleport requests", "tpa");
    addAliases("teleportaccept");
    addSubcommand(Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
        "Accept a teleport request with a player's username");
    setEnabler(Settings.ENABLE_TELEPORTING);
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may execute that command!"));
      return false;
    }
    Player destination = (Player) sender;

    if (args.length < 1) {
      sendCommandError(sender, CommandError.FEW_ARGUMENTS);
      return false;
    }

    External.getPlayerUuid(args[0]).thenAccept(uuid -> {
      if (!uuid.isPresent()) {
        sendCommandError(sender, CommandError.NO_PLAYER);
        return;
      }
      Player requester = Bukkit.getPlayer(uuid.get());
      if (requester == null) {
        sender.sendMessage(Format.error("The server couldn't find that player!"));
        return;
      }
      TeleportManager tpManager = Workshop.getInstance().getState().getTeleportManager();
      if (!tpManager.hasRequest(requester.getUniqueId(), uuid.get())) {
        sender.sendMessage(Format.error("There is no incoming request from that player"));
        return;
      }
      if (tpManager.expired(uuid.get(), destination.getUniqueId())) {
        sender.sendMessage(Format.error("That request has expired!"));
        return;
      }
      tpManager.accept(requester.getUniqueId(), uuid.get());
      requester.sendMessage(Format.success("Your request was accepted!"));
      destination.sendMessage(Format.success("You have accepted the request"));
    });
    return true;
  }
}
