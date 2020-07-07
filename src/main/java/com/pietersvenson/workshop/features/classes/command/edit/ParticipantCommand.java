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

package com.pietersvenson.workshop.features.classes.command.edit;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandError;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.features.classes.Participant;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ParticipantCommand extends CommandTree.CommandNode {


  public ParticipantCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Manage the participant list of a class",
        "participant");
    addChildren(new ParticipantAddCommand(this));
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    sendCommandError(sender, CommandError.FEW_ARGUMENTS);
    return false;
  }

  public static class ParticipantAddCommand extends CommandTree.CommandNode {

    public ParticipantAddCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Add a participant to this class",
          "add");
      addAliases("a");
      addSubcommand(Parameter.chain(
          Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          Parameter.builder().supplier(ParameterSuppliers.ONLINE_PLAYER).build(),
          Parameter.basic("<first>"),
          Parameter.basic("<last>")),
          "The username of the participant");

    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      if (args.length < 4) {
        sendCommandError(sender, CommandError.FEW_ARGUMENTS);
        return false;
      }
      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      Optional<Classroom> classroom = manager.getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage(Format.error("That class doesn't exist yet!"));
        return false;
      }
      if (!Validate.isName(args[2]) || !Validate.isName(args[3])) {
        sender.sendMessage(Format.error("First and last names must be comprised of only letters and hyphens"));
        return false;
      }

      External.getPlayer(args[1]).thenAccept(uuid -> {
        if (!uuid.isPresent()) {
          sender.sendMessage(Format.error("No player exists with that username."));
          return;
        }
        classroom.get().addParticipant(new Participant(args[2], args[3], uuid.get()));
        sender.sendMessage(Format.success("Player added!"));
      });
      return true;
    }
  }

}
