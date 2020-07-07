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
import com.pietersvenson.workshop.features.classes.Curriculum;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CurriculumCommand extends CommandTree.CommandNode {

  public CurriculumCommand(@Nullable CommandTree.CommandNode parent) {
    super(parent,
        Permissions.STAFF,
        "Edits the curriculum of the given class",
        "curriculum");
    addSubcommand(Parameter.builder()
            .supplier(ParameterSuppliers.CLASS_ID)
            .next(Parameter.builder()
                .supplier(ParameterSuppliers.CURRICULUM)
                .build())
            .build(),
        "The current name of this class");
  }

  @Override
  public boolean onWrappedCommand(@Nonnull CommandSender sender,
                                  @Nonnull Command command,
                                  @Nonnull String label,
                                  @Nonnull String[] args) {
    ClassroomManager manager = Workshop.getInstance().getState().getClassroomManager();
    if (args.length < 2) {
      sendCommandError(sender, CommandError.FEW_ARGUMENTS);
      return false;
    }
    Optional<Classroom> classroom = manager.getClassroom(args[0]);
    if (!classroom.isPresent()) {
      sender.sendMessage(Format.error("That class doesn't exist yet!"));
      return false;
    }
    Curriculum curriculum;
    try {
      curriculum = Curriculum.valueOf(args[1].toUpperCase());
    } catch (IllegalArgumentException e) {
      sender.sendMessage(Format.warn("That curriculum isn't currently supported."));
      return false;
    }
    classroom.get().setCurriculum(curriculum);
    sender.sendMessage(Format.success("Curriculum set!"));
    return true;
  }

}
