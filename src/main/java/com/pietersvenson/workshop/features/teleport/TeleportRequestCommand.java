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
import com.pietersvenson.workshop.command.common.CommandTree;
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

public class TeleportRequestCommand extends CommandTree.CommandNode {


  public TeleportRequestCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent, Permissions.COMMAND_ROOT, "Request teleports", "tpr");
    addAliases("tp");
    addAliases("teleport");
    addSubcommand(Parameter.builder()
            .supplier(ParameterSuppliers.ONLINE_PLAYER)
            .build(),
        "Request a teleport to another online player");
    setEnabler(Settings.ENABLE_TELEPORTING::getValue);
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
    Player requester = (Player) sender;

    if (args.length < 1) {
      sendCommandError(sender, CommandError.FEW_ARGUMENTS);
      return false;
    }

    External.getPlayerUuid(args[0]).thenAccept(uuid -> {
      if (!uuid.isPresent()) {
        sendCommandError(sender, CommandError.NO_PLAYER);
        return;
      }
      if (uuid.get().equals(requester.getUniqueId())) {
        sendCommandError(sender, "You can't request an teleport from yourself!");
        return;
      }
      Player destination = Bukkit.getPlayer(uuid.get());
      if (destination == null) {
        sender.sendMessage(Format.error("The server couldn't find that player!"));
        return;
      }
      Workshop.getInstance().getState().getTeleportManager().request(requester.getUniqueId(), uuid.get());
      requester.sendMessage(Format.success("Request sent!"));
      destination.sendMessage(Format.success(requester.getName() + " has requested to teleport to you"));
    });
    return true;

  }

}
