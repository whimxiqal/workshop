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
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pietersvenson.workshop.util.Randomer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand extends CommandTree.CommandNode {

  /**
   * Default constructor.
   *
   * @param parent the parent node
   */
  public FreezeCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Immobilize players",
        "freeze");
    addSubcommand(Parameter.chain(
        Parameter.builder()
            .supplier(ParameterSuppliers.ONLINE_PLAYER)
            .permission(Permissions.STAFF)
            .build(),
        Parameter.builder()
            .supplier(ParameterSuppliers.BOOLEAN)
            .permission(Permissions.STAFF)
            .build()),
        "Freeze a specific player");
    addChildren(new FreezeAllCommand(this));
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    if (args.length == 0) {
      sendCommandError(sender, "Too few arguments!");
      return false;
    }
    FreezeManager freezeManager = Workshop.getInstance().getState().getFreezeManager();
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getName().equalsIgnoreCase(args[0])) {
        if (args.length == 1) {
          if (freezeManager.isFrozen(player)) {
            freezeManager.unfreeze(player);
            sender.sendMessage(Format.success("Unfroze " + player.getName()));
          } else {
            if (player.hasPermission(Permissions.STAFF)) {
              sender.sendMessage(Format.error("You can't freeze staff!"));
            } else {
              freezeManager.freeze(player);
              sender.sendMessage(Format.success("Froze " + player.getName()));
            }
          }
        } else {
          if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("t")) {
            if (player.hasPermission(Permissions.STAFF)) {
              sender.sendMessage(Format.error("You can't freeze staff!"));
              return false;
            }
            if (freezeManager.freeze(player)) {
              sender.sendMessage(Format.success("Froze " + player.getName()));
            } else {
              sender.sendMessage(Format.error(
                  Randomer.word(Randomer.WordType.ERROR)
                      + ". Are they already frozen?"));
            }
          } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("f")) {
            if (freezeManager.unfreeze(player)) {
              sender.sendMessage(Format.success("Unfroze " + player.getName()));
            } else {
              sender.sendMessage(Format.error(
                  Randomer.word(Randomer.WordType.ERROR)
                  + ". Are they already unfrozen?"));
            }
          } else {
            sendCommandError(sender, "Invalid boolean");
            return false;
          }
        }
        return true;
      }
    }
    sendCommandError(sender, "No player matches that name!");
    return false;
  }

  private static class FreezeAllCommand extends CommandTree.CommandNode {

    public FreezeAllCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent, Permissions.STAFF, "Freeze all players", "all");
      addAliases("a");
      addSubcommand(Parameter.builder()
          .supplier(ParameterSuppliers.BOOLEAN)
          .permission(Permissions.STAFF)
          .build(), "Explicitly immobilize all players");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      FreezeManager freezeManager = Workshop.getInstance().getState().getFreezeManager();

      if (args.length == 0) {
        if (freezeManager.isAllFrozen()) {
          freezeManager.unfreezeAll();
          sender.sendMessage(Format.success("Unfroze all players"));
        } else {
          freezeManager.freezeAll();
          sender.sendMessage(Format.success("Froze all players"));
        }
      } else {
        if (args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("t")) {
          freezeManager.freezeAll();
          sender.sendMessage(Format.success("Froze all players"));
        } else if (args[0].equalsIgnoreCase("false") || args[0].equalsIgnoreCase("f")) {
          freezeManager.unfreezeAll();
          sender.sendMessage(Format.success("Unfroze all players"));
        } else {
          sendCommandError(sender, "Invalid boolean");
          return false;
        }
      }
      return true;
    }
  }
}
