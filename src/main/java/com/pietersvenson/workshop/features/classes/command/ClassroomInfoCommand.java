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
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Appointment;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class ClassroomInfoCommand extends CommandNode {

  public ClassroomInfoCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.STAFF,
        "Display the info about a class",
        "info");
    addChildren(
        new ClassroomInfoParticipantsCommand(this),
        new ClassroomInfoScheduleCommand(this));
    addSubcommand(Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
        "Use the class id to get the info");
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
    ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
    Optional<Classroom> classroom = manager.getClassroom(args[0]);
    if (!classroom.isPresent()) {
      sender.sendMessage(Format.error("That class doesn't exist"));
      return false;
    }

    sender.sendMessage(Format.success("Class " + args[0] + ":"));
    sender.sendMessage(Format.PREFIX + ChatColor.AQUA + "Name: "
        + ChatColor.RESET
        + classroom.get().getName());
    sender.sendMessage(Format.PREFIX + ChatColor.AQUA + "Curriculum: "
        + ChatColor.RESET
        + classroom.get().getCurriculum());
    sender.sendMessage(Format.PREFIX + ChatColor.AQUA + "Participants: "
        + ChatColor.RESET
        + classroom.get().getParticipants().size());
    sender.sendMessage(Format.PREFIX + ChatColor.AQUA + "Schedule: "
        + ChatColor.RESET
        + classroom.get().getSchedule().getAppointments().size() + " appointments");

    return true;
  }

  public static final class ClassroomInfoParticipantsCommand extends CommandNode {

    public ClassroomInfoParticipantsCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.STAFF,
          "Display the participants of a class",
          "participants");
      addSubcommand(Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          "Use the class id to get the participants");
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
      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      Optional<Classroom> classroom = manager.getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage(Format.error("That class doesn't exist"));
        return false;
      }

      if (classroom.get().getParticipants().isEmpty()) {
        sender.sendMessage(Format.success("No participants are currently registered"));
      } else {
        sender.sendMessage(Format.success("Class " + args[0] + " Participants:"));
        classroom.get().getParticipants().forEach(part ->
            External.getPlayerName(part.getPlayerUuid()).thenAccept(nameOp ->
                sender.sendMessage(Format.PREFIX
                    + ChatColor.GRAY + "- "
                    + ChatColor.AQUA + part.getFirstName() + " " + part.getLastName()
                    + nameOp.map(name -> ChatColor.GRAY + ", " + name).orElse(""))));
      }
      return true;
    }

  }

  public static final class ClassroomInfoScheduleCommand extends CommandNode {

    public ClassroomInfoScheduleCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.STAFF,
          "Display the schedule of a class",
          "schedule");
      addSubcommand(Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          "Use the class id to get the schedule");
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
      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      Optional<Classroom> classroom = manager.getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage(Format.error("That class doesn't exist"));
        return false;
      }

      List<Appointment> appointments = classroom.get().getSchedule().getAppointments();
      if (appointments.isEmpty()) {
        sender.sendMessage(Format.success("There are currently no appointments"));
      } else {
        sender.sendMessage(Format.success("Class " + args[0] + " Schedule:"));
        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
        IntStream.range(0, appointments.size()).forEach(
            i -> sender.sendMessage(Format.PREFIX
                + ChatColor.GRAY + (i + 1) + ". "
                + ChatColor.AQUA + df.format(Date.from(appointments.get(i).getStart()))
                + ChatColor.GRAY + " --> "
                + ChatColor.AQUA + df.format(Date.from(appointments.get(i).getEnd()))));
      }

      return true;
    }

  }

}
