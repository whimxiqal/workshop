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

import com.google.common.collect.Lists;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.command.common.CommandError;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParticipantsCommand extends CommandTree.CommandNode {


  public ParticipantsCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Manage the participant list of a class",
        "participants");
    addChildren(new ParticipantsAddCommand(this),
        new ParticipantsRemoveCommand(this));
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    sendCommandError(sender, CommandError.FEW_ARGUMENTS);
    return false;
  }

  public static class ParticipantsAddCommand extends CommandTree.CommandNode {

    public ParticipantsAddCommand(@Nullable CommandTree.CommandNode parent) {
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
          "Add a participant using their personal information");

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
        sender.sendMessage(Format.error("That class doesn't exist!"));
        return false;
      }
      if (!Validate.isName(args[2]) || !Validate.isName(args[3])) {
        sender.sendMessage(Format.error("First and last names must be comprised of only letters and hyphens"));
        return false;
      }

      External.getPlayerUuid(args[1]).thenAccept(uuid -> {
        if (!uuid.isPresent()) {
          sender.sendMessage(Format.error("No player exists with that username"));
          return;
        }
        Classroom.Participant out = new Classroom.Participant(args[2], args[3], uuid.get());
        if (classroom.get().isRegistered(uuid.get())) {
          sender.sendMessage(Format.error("That player is already registered!"));
          return;
        }
        // Set player with correct capitalization (because the argument might not have had it)
        External.getPlayerName(out.getPlayerUuid())
            .thenAccept(nameOp ->
                nameOp.ifPresent(out::setLastKnownUsername));
        classroom.get().addParticipant(out);
        sender.sendMessage(Format.success("Player added!"));
        Workshop.getInstance().saveStateSynchronous();
      });
      return true;
    }
  }

  public static class ParticipantsRemoveCommand extends CommandTree.CommandNode {

    public ParticipantsRemoveCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Remove a participant from this class",
          "remove");
      addAliases("r");
      addSubcommand(
          Parameter.chain(
              Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
              Parameter.builder()
                  .supplier(Parameter.ParameterSupplier.builder()
                      .allowedEntries(prev -> {
                        if (prev.size() < 1) {
                          return Lists.newLinkedList();
                        }
                        return Workshop.getInstance().getState()
                            .getClassroomManager()
                            .getClassroom(prev.get(0))
                            .map(classroom -> classroom.getParticipants().stream()
                                .map(Classroom.Participant::getLastKnownUsername)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                            .orElse(Lists.newLinkedList());
                      })
                      .build())
                  .build()),
          "Remove a participant using the class id and the username");

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
      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      Optional<Classroom> classroom = manager.getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage(Format.error("That class doesn't exist!"));
        return false;
      }

      External.getPlayerUuid(args[1]).thenAccept(uuid -> {
        if (!uuid.isPresent()) {
          sender.sendMessage(Format.error("No player exists with that username."));
          return;
        }
        if (classroom.get().removeParticipant(uuid.get())) {
          sender.sendMessage(Format.success("Player removed!"));
        } else {
          sender.sendMessage(Format.error("That player isn't in the class!"));
        }
      });
      return true;
    }
  }

}
