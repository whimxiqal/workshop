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
import com.pietersvenson.workshop.features.classes.Appointment;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassroomDismissCommand extends CommandNode {
  public ClassroomDismissCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Kick all students registered for the current class",
        "dismiss");
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
    Optional<Classroom> classroom = Workshop.getInstance().getState().getClassroomManager().getInSession();
    if (!classroom.isPresent()) {
      sender.sendMessage(Format.error("There are no classes currently in session"));
      return false;
    }
    List<Player> toKick = Bukkit.getOnlinePlayers().stream()
        .filter(player -> !player.hasPermission(Permissions.STAFF))
        .filter(player -> classroom.get().getParticipants().stream().anyMatch(part -> part.getPlayerUuid().equals(player.getUniqueId())))
        .collect(Collectors.toList());
    for (Player player : toKick) {
      Optional<Appointment> next = classroom.get().getSchedule().nextAppointment();
      player.kickPlayer("Class has been dismissed!\n"
          + (next.isPresent() ? "See you next time!" : "We hope you enjoyed the class!"));
    }
    sender.sendMessage(Format.success("All participants have been kicked!"));
    return true;
  }
}
