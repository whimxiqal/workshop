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
import com.pietersvenson.workshop.features.classes.command.ClassroomCommand;
import com.pietersvenson.workshop.features.freeze.FreezeCommand;
import com.pietersvenson.workshop.features.home.HomeCommand;
import com.pietersvenson.workshop.features.nickname.NicknameCommand;
import com.pietersvenson.workshop.features.noitem.NoitemCommand;
import com.pietersvenson.workshop.features.spawn.SpawnCommand;
import com.pietersvenson.workshop.features.teleport.TeleportAcceptCommand;
import com.pietersvenson.workshop.features.teleport.TeleportRequestCommand;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Reference;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public final class WorkshopCommandRoot extends CommandTree.CommandNode {

  public static final String DESCRIPTION = "The root command for all Workshop related commands";
  public static final Permission PERMISSION = Permissions.COMMAND_ROOT;

  /**
   * Default constructor.
   */
  public WorkshopCommandRoot() {
    super(
        null,
        PERMISSION,
        DESCRIPTION,
        "workshop");
    addAliases("ws");
    addChildren(new FreezeCommand(this),
        new HomeCommand(this),
        new NoitemCommand(this),
        new ClassroomCommand(this),
        new ReloadCommand(this),
        new NicknameCommand(this),
        new SpawnCommand(this),
        new TeleportAcceptCommand(this),
        new TeleportRequestCommand(this)
    );
  }

  @Override
  final public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    // TODO: format plugin splash screen
    if (args.length > 0) {
      sendCommandError(sender, "Unknown command.");
      return false;
    }
    sender.sendMessage(ChatColor.GRAY + "# " + Format.THEME + "Workshop " + ChatColor.GRAY + "v." + Reference.VERSION + " #");
    return true;
  }

}
