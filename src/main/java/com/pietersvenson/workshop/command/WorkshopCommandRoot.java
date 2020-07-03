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

package com.pietersvenson.workshop.command;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.features.freeze.FreezeCommand;
import com.pietersvenson.workshop.features.home.HomeCommand;
import com.pietersvenson.workshop.features.noitem.NoitemCommand;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Reference;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class WorkshopCommandRoot extends CommandTree.CommandNode {

  public static final String DESCRIPTION = "The root command for all Workshop related commands";
  public static final Permission PERMISSION = Permissions.COMMAND_ROOT;

  public WorkshopCommandRoot() {
    super(
        null,
        PERMISSION,
        DESCRIPTION,
        "workshop");
    addAliases("ws");
    addChildren(new FreezeCommand(this));
    addChildren(new HomeCommand(this));
    addChildren(new NoitemCommand(this));
  }

  @Override
  public boolean onWrappedCommand(CommandSender sender, Command command, String label, String[] args) {
    // TODO: format plugin splash screen
    if (args.length > 0) {
      sendCommandError(sender, "Unknown command.");
      return false;
    }
    sender.sendMessage(ChatColor.GRAY + "# " + Format.THEME + "Workshop " + ChatColor.GRAY + "v." + Reference.VERSION + " #");
    sender.sendMessage(ChatColor.GRAY + "Author(s): " + ChatColor.AQUA + "Pieter Svenson");

    // TODO remove this:
    Workshop.getInstance().getState().save();
    return true;
  }

}
