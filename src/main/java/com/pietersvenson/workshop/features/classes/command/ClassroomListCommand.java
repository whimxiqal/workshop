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
import com.pietersvenson.workshop.command.common.CommandNode;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.features.classes.Schedule;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class ClassroomListCommand extends CommandNode {
  public ClassroomListCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "List all registered classes",
        "list");
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
    if (manager.getClassroomIds().isEmpty()) {
      sender.sendMessage(Format.success("No classes have been created yet"));
    } else {
      sender.sendMessage(Format.success("Classes:"));
      for (String id : manager.getClassroomIds()) {
        if (!manager.getClassroom(id).isPresent()) {
          continue;
        }
        Schedule schedule = manager.getClassroom(id).get().getSchedule();

        // TODO verify formatting
        String out = ChatColor.GRAY + "- " + ChatColor.AQUA + id + ": " + ChatColor.RESET;
        if (schedule.getStatus() == Schedule.Status.EMPTY) {
          out += "No appointments";
        } else if (schedule.getStatus() == Schedule.Status.PRE) {
          out += "Starts on " + new SimpleDateFormat("yy/MM/dd").format(Date.from(schedule.getAppointments().getFirst().getStart()))
              + "; "
              + schedule.getAppointments().size() + " appointments";
        } else if (schedule.getStatus() == Schedule.Status.DURING) {
          out += "In Progress; " + schedule.getAppointments().stream().filter(app -> Instant.now().isBefore(app.getEnd())).count() + " appointments remaining";
        } else {
          out += "Completed on " + new SimpleDateFormat("yy/MM/dd").format(Date.from(schedule.getAppointments().getLast().getEnd()));
        }
        sender.sendMessage(out);
      }
    }
    return true;
  }
}
