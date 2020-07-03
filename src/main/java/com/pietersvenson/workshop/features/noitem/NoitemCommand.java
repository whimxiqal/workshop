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

package com.pietersvenson.workshop.features.noitem;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pietersvenson.workshop.util.Randomer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class NoitemCommand extends CommandTree.CommandNode {

  /**
   * Default constructor.
   *
   * @param parent parent node
   */
  public NoitemCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Ban certain items",
        "noitem");
    addInput(Parameter.builder()
        .supplier(ParameterSuppliers.ITEM)
        .permission(Permissions.STAFF)
        .build(), "Ban an item");
    addChildren(new NoitemListCommand(this));
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
    Material material;

    try {
      material = Material.matchMaterial(args[0], false);
    } catch (Exception e) {
      sendCommandError(sender, "Error!");
      e.printStackTrace();
      return false;
    }

    if (material == null) {
      sendCommandError(sender, "That item doesn't exist!");
      return false;
    } else {
      Workshop.getInstance().getLogger().info("Material: " + material.name());
    }

    if (!material.isItem()) {
      sendCommandError(sender, "That type isn't an item!");
      return false;
    }


    NoitemManager noitemManager = Workshop.getInstance().getState().getNoitemManager();
    if (noitemManager.isBanned(material)) {
      noitemManager.unban(material);
        sender.sendMessage(Format.success("The item "
            + material.toString() + " is now "
            + ChatColor.YELLOW + "unbanned"));
    } else {
      noitemManager.ban(material);
      sender.sendMessage(Format.success("The item "
          + material.toString() + " is now "
          + ChatColor.RED + "banned"));
    }
    return true;
  }

  public static class NoitemListCommand extends CommandTree.CommandNode {

    NoitemListCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "List all banned items",
          "list");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
      List<Material> banned = Workshop.getInstance().getState().getNoitemManager().getBanned();
      if (banned.isEmpty()) {
        sender.sendMessage(Format.success("No items are banned."));
      } else {
        sender.sendMessage(Format.success("Banned items: "
            + ChatColor.WHITE
            + banned.stream().map(Material::name).collect(Collectors.joining(", "))));
      }
      return true;
    }
  }

}
