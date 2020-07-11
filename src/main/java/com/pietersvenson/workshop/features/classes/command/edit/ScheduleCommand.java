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
import com.pietersvenson.workshop.command.common.FunctionlessCommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Appointment;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.features.classes.Schedule;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScheduleCommand extends FunctionlessCommandNode {

  public ScheduleCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Edits the schedule of the given class",
        "schedule");
    addChildren(
        new ScheduleAddCommand(this),
        new ScheduleRemoveCommand(this));
  }

  static final class ScheduleRemoveCommand extends CommandTree.CommandNode {

    public ScheduleRemoveCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Add a new schedule with some preset",
          "remove");
      addSubcommand(Parameter.chain(
          Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .allowedEntries(prev -> {
                    if (prev.size() < 1) {
                      return Lists.newLinkedList();
                    }
                    Optional<Classroom> classroom = Workshop.getInstance()
                        .getState()
                        .getClassroomManager()
                        .getClassroom(prev.get(0));
                    return classroom.map(value ->
                        IntStream.range(1, value.getSchedule().getAppointments().size() + 1)
                            .boxed()
                            .map(Object::toString)
                            .collect(Collectors.toList()))
                        .orElseGet(Lists::newLinkedList);
                  })
                  .usage("<appt-id>")
                  .build())
              .build()), "Remove an appointment using its index");
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
      Optional<Classroom> classroom = Workshop.getInstance()
          .getState()
          .getClassroomManager()
          .getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage("That class doesn't exist!");
        return false;
      }
      if (classroom.get().getSchedule().getAppointments().isEmpty()) {
        sender.sendMessage("There are no appointments scheduled!");
        return false;
      }
      try {
        classroom.get().getSchedule().cancel(Integer.parseInt(args[1]) - 1);
      } catch (NumberFormatException e) {
        sendCommandError(sender, "The index must be an integer");
        return false;
      } catch (IndexOutOfBoundsException e) {
        sendCommandError(sender, "The index must be between 1 and "
            + (classroom.get().getSchedule().getAppointments().size()));
        return false;
      }

      sender.sendMessage(Format.success("That appointment was canceled"));
      return true;
    }
  }

  static final class ScheduleAddCommand extends FunctionlessCommandNode {

    public static final int APPOINTMENT_CREATION_LIMIT = 30;

    public ScheduleAddCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Add a new schedule with some preset",
          "add");
      addChildren(
          new ScheduleAddPeriodicCommand(this,
              Permissions.STAFF,
              "Add a new schedule with the daily preset",
              "daily",
              Duration.of(1, ChronoUnit.DAYS)),
          new ScheduleAddPeriodicCommand(this,
              Permissions.STAFF,
              "Add a new schedule with the weekly preset",
              "weekly",
              Duration.of(7, ChronoUnit.DAYS)),
          new ScheduleAddSingleCommand(this));
    }

  }

  static final class ScheduleAddPeriodicCommand extends CommandTree.CommandNode {

    Duration period;

    public ScheduleAddPeriodicCommand(@Nullable CommandTree.CommandNode parent,
                                      @Nonnull Permission permission,
                                      @Nonnull String description,
                                      @Nonnull String alias,
                                      @Nonnull Duration period) {
      super(parent, permission, description, alias);
      this.period = Objects.requireNonNull(period);
      addSubcommand(Parameter.chain(
          Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_START_DATE).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_END_DATE).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_START_TIME).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_END_TIME).build()
      ), "Set the schedule of this class to be a daily event, dates inclusive. Date: yy/MM/dd, Time: HH:mm");
    }

    @Override
    public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                    @Nonnull Command command,
                                    @Nonnull String label,
                                    @Nonnull String[] args) {
      if (args.length < 5) {
        sendCommandError(sender, CommandError.FEW_ARGUMENTS);
        return false;
      }

      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      Optional<Classroom> classroom = manager.getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage("There is no class with that id!");
        return false;
      }

      try {
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm");
        Instant firstClassStart = format.parse(args[1] + " " + args[3]).toInstant();
        Instant firstClassEnd = format.parse(args[1] + " " + args[4]).toInstant();
        if (firstClassStart.isAfter(firstClassEnd)) {
          sendCommandError(sender, "You can't have the start time be after the end time!");
          return false;
        }
        Instant lastClassStart = format.parse(args[2] + " " + args[3]).toInstant();

        // TODO use duration time here instead of statically days
        int dayCount = new Long((lastClassStart.toEpochMilli() - firstClassStart.toEpochMilli()) / period.toMillis()).intValue();
        if (dayCount > ScheduleAddCommand.APPOINTMENT_CREATION_LIMIT) {
          sender.sendMessage(Format.error("You can't make more than "
              + ScheduleAddCommand.APPOINTMENT_CREATION_LIMIT
              + " appointments at once!"));
          return false;
        }
        Schedule toAdd = Schedule.repeating(
            new Appointment(firstClassStart, firstClassEnd),
            period,
            dayCount);
        return attemptToAddSchedule(this, sender, classroom.get().getSchedule(), toAdd);
      } catch (ParseException e) {
        sendCommandError(sender, "Incorrect input format!");
        return false;
      } catch (Schedule.OverlappingAppointmentException e) {
        // Just verify here, but it shouldn't be called
        sendCommandError(sender, "That operation would create conflicting appointments!");
        return false;
      }
    }
  }

  static final class ScheduleAddSingleCommand extends CommandTree.CommandNode {

    public ScheduleAddSingleCommand(@Nullable CommandTree.CommandNode parent) {
      super(parent,
          Permissions.STAFF,
          "Add a single appointment to the current schedule",
          "single");
      addSubcommand(Parameter.chain(
          Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_START_DATE).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_START_TIME).build(),
          Parameter.builder().supplier(ParameterSuppliers.CLASS_END_TIME).build()
      ), "Collect information into a single appointment and add to schedule");
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

      Optional<Classroom> classroom = Workshop.getInstance().getState().getClassroomManager().getClassroom(args[0]);
      if (!classroom.isPresent()) {
        sender.sendMessage("There is no class with that id!");
        return false;
      }

      try {
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm");
        Instant classStart = format.parse(args[1] + " " + args[2]).toInstant();
        Instant classEnd = format.parse(args[1] + " " + args[3]).toInstant();
        if (classStart.isAfter(classEnd)) {
          classEnd = classEnd.plus(1, ChronoUnit.DAYS);
        }
        Schedule toAdd = Schedule.single(new Appointment(classStart, classEnd));
        return attemptToAddSchedule(this, sender, classroom.get().getSchedule(), toAdd);
      } catch (ParseException e) {
        sendCommandError(sender, "Incorrect input format!");
        return false;
      }
    }
  }

  private static boolean attemptToAddSchedule(CommandTree.CommandNode node, CommandSender sender, Schedule first, Schedule second) {
    try {
      if (first.overlaps(second)) {
        node.sendCommandError(sender, "That operation would create a conflicting appointment!");
        return false;
      }
      ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
      for (Optional<Classroom> c : Workshop.getInstance().getState()
          .getClassroomManager()
          .getClassroomIds()
          .stream()
          .map(manager::getClassroom)
          .collect(Collectors.toList())) {
        if (c.isPresent() && c.get().getSchedule().overlaps(second)) {
          node.sendCommandError(sender, "That operation would create conflicting classes!");
          return false;
        }
      }
      first.add(second);
      sender.sendMessage(Format.success("Schedule added!"));
      return true;
    } catch (Schedule.OverlappingAppointmentException e) {
      // Just verify here, but it shouldn't be called
      node.sendCommandError(sender, "That operation would create conflicting appointments!");
      return false;
    }
  }

}
