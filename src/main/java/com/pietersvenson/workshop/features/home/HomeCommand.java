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

package com.pietersvenson.workshop.features.home;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

public class HomeCommand extends CommandNode {

  /**
   * Default constructor.
   *
   * @param parent the parent node
   */
  public HomeCommand(@Nonnull CommandNode parent) {
    super(parent,
        Permissions.COMMAND_ROOT,
        "Teleport to saved locations",
        "home");
    addChildren(new HomeSetCommand(this));
    addSubcommand(Parameter.builder()
            .supplier(ParameterSuppliers.NONE)
            .permission(Permissions.COMMAND_ROOT)
            .build(),
        "Teleport to your home.");
    addSubcommand(Parameter.builder()
            .supplier(ParameterSuppliers.ONLINE_PLAYER)
            .permission(Permissions.STAFF)
            .build(),
        "Teleport to the home of <player>");
    setEnabler(Settings.ENABLE_HOMES);
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

    if (args.length == 0) {
      Optional<Location> home = Workshop.getInstance().getState().getHomeManager().getHome(player);
      if (!home.isPresent()) {
        player.sendMessage(Format.error("You don't have a home set yet! Use "
            + ChatColor.GRAY + "/"
            + Workshop.getInstance().getWorkshopCommandTree().getNode(HomeSetCommand.class).get().getFullCommand()
            + Format.ERROR
            + " to set it."));
        return false;
      }

      player.teleport(home.get());
      player.sendMessage(Format.success("Welcome home!"));
      return true;

    } else if (args.length == 1) {
      if (player.hasPermission(Permissions.STAFF)) {
        for (Map.Entry<String, Location> homeEntry : Workshop.getInstance().getState().getHomeManager().getHomesByName().entrySet()) {
          if (homeEntry.getKey().equalsIgnoreCase(args[0])) {
            ((Player) sender).teleport(homeEntry.getValue());
            sender.sendMessage(Format.success("Teleported to " + homeEntry.getKey() + "'s home."));
            return true;
          }
        }
        player.sendMessage(Format.error("That player doesn't have a home!"));
        return false;
      }
    }

    sendCommandError(sender, "Incorrect usage.");
    return false;
  }

  private static class HomeSetCommand extends CommandNode {

    private HomeSetCommand(@Nonnull HomeCommand parent) {
      super(parent,
          Permissions.COMMAND_ROOT,
          "Set the location of your home",
          "set");
      addAliases("s");
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
      Workshop.getInstance().getState().getHomeManager().setHome(player, player.getLocation());
      player.sendMessage(Format.success("Home set!"));
      return true;
    }

  }

}
