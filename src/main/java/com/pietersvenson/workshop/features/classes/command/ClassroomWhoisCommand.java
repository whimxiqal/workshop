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

package com.pietersvenson.workshop.features.classes.command;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandError;
import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.command.common.LambdaCommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ClassroomWhoisCommand extends CommandNode {

  public ClassroomWhoisCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.STAFF,
        "Find out what the name is of a player in a class",
        "whois");
    addAliases("who");
    addSubcommand(Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
        "Use the player's username to find their registered IRL name");
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
    Optional<Classroom> classroom = Workshop.getInstance().getState().getClassroomManager().getInSession();
    if (!classroom.isPresent()) {
      sender.sendMessage(Format.error("There are no classes currently in session"));
      return false;
    }
    External.getPlayerUuid(args[0]).thenAccept(uuid -> {
      if (!uuid.isPresent()) {
        sendCommandError(sender, CommandError.NO_PLAYER);
        return;
      }
      Optional<Classroom.Participant> participant = classroom.get().getParticipants()
          .stream()
          .filter(part -> part.getPlayerUuid().equals(uuid.get()))
          .findAny();
      if (!participant.isPresent()) {
        sender.sendMessage(Format.error("There is no registered participant in the current class with that username"));
        return;
      }
      sender.sendMessage(Format.PREFIX
          + ChatColor.LIGHT_PURPLE + args[0]
          + Format.INFO + " is "
          + ChatColor.GREEN + participant.get().getFirstName() + " "
          + participant.get().getLastName());
    });
    return true;
  }
}
