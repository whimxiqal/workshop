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
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClassroomCreateCommand extends CommandNode {

  public ClassroomCreateCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Establish a new class",
        "create");
    addAliases("c");
    addSubcommand(Parameter.basic("<id>"), "Id of the class, which cannot be changed.");

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
    if (!Validate.isKebabCase(args[0])) {
      sendCommandError(sender, "The id of this class must only have lower case letters, numbers, and hyphens.");
      return false;
    }
    ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
    if (manager.getClassroom(args[0]).isPresent()) {
      sendCommandError(sender, "A classroom by this name already exists.");
      return false;
    }
    Classroom classroom = new Classroom(args[0]);
    manager.addClassroom(classroom);
    sender.sendMessage(Format.success("You created the class "
        + Format.ACCENT_1 + args[0]
        + Format.SUCCESS + ". Use other class commands to add a schedule and participants. "
        + "This class is currently " + (classroom.isPublic() ? "public" : "private") + "."));
    return true;
  }

}
