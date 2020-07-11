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

package com.pietersvenson.workshop.features.nickname;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandError;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.command.common.FunctionlessCommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NicknameCommand extends FunctionlessCommandNode {


  public NicknameCommand(@Nullable CommandTree.CommandNode parent, boolean active) {
    super(parent,
        Permissions.STAFF,
        "Manage nicknames in the server",
        "nickname", true, active);
    addAliases("nick");
    addChildren(new NicknameSetCommand(this),
        new NicknameRemoveCommand(this));
    setEnabler(Settings.ENABLE_NICKNAME_COMMAND::getValue);
  }

  public static final class NicknameSetCommand extends CommandTree.CommandNode {

    public NicknameSetCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Set players' nicknames",
          "set");
      addSubcommand(Parameter.chain(
          Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
          Parameter.basic("<new>")),
          "Sets the nickname of the given player");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      if (args.length < 2) {
        sendCommandError(sender, CommandError.FEW_ARGUMENTS);
        return false;
      }

      if (!Validate.isAlphaNumeric(args[1])) {
        sender.sendMessage(Format.error("Nicknames may only contain letters and numbers."));
        return false;
      }

      External.getPlayerUuid(args[0]).thenAccept(uuid -> {
        if (!uuid.isPresent()) {
          sendCommandError(sender, CommandError.NO_PLAYER);
          return;
        }
        try {
          Workshop.getInstance().getState().getNicknameManager().setNickname(uuid.get(), args[1] + ChatColor.RESET);
          sender.sendMessage(Format.success("Nickname set!"));
        } catch (UnsupportedOperationException e) {
          sender.sendMessage(Format.error("That command is not currently supported"));
        }
      });
      return true;
    }
  }

  public static final class NicknameRemoveCommand extends CommandTree.CommandNode {

    public NicknameRemoveCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Removes players' nicknames",
          "remove");
      addSubcommand(Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
          "Resets the nickname of the given player to their username");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      if (args.length < 1) {
        sendCommandError(sender, CommandError.FEW_ARGUMENTS);
        return false;
      }

      External.getPlayerUuid(args[0]).thenAccept(uuid -> {
        if (!uuid.isPresent()) {
          sendCommandError(sender, CommandError.NO_PLAYER);
          return;
        }
        try {
          if (!Workshop.getInstance().getState().getNicknameManager().hasNickname(uuid.get())) {
            sender.sendMessage(Format.error("That player doesn't have a nickname!"));
          }
          Workshop.getInstance().getState().getNicknameManager().removeNickname(uuid.get());
        } catch (UnsupportedOperationException e) {
          sender.sendMessage(Format.error("That command is not currently supported"));
        }
        sender.sendMessage(Format.success("Nickname removed!"));
      });
      return true;
    }
  }

//  public static final class NicknameWhoCommand extends CommandTree.CommandNode {
//
//    public NicknameWhoCommand(@Nullable CommandTree.CommandNode parent) {
//      super(parent,
//          Permissions.STAFF,
//          "Check players' real usernames",
//          "who");
//      addSubcommand(Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
//          "Resets the nickname of the given player to their username");
//    }
//
//    @Override
//    public boolean onWrappedCommand(@Nonnull CommandSender sender,
//                                    @Nonnull Command command,
//                                    @Nonnull String label,
//                                    @Nonnull String[] args) {
//      if (args.length < 1) {
//        sendCommandError(sender, CommandError.FEW_ARGUMENTS);
//        return false;
//      }
//
//      External.getPlayerUuid(args[0]).thenAccept(uuid -> {
//        if (!uuid.isPresent()) {
//          sendCommandError(sender, CommandError.NO_PLAYER);
//          return;
//        }
//        if (!Workshop.getInstance().getState().getNicknameManager().hasNickname(uuid.get())) {
//          sender.sendMessage(Format.error("That player doesn't have a nickname!"));
//        }
//        Workshop.getInstance().getState().getNicknameManager().resetNickname(uuid.get());
//        sender.sendMessage(Format.success("Nickname removed!"));
//      });
//      return true;
//    }
//  }

}
