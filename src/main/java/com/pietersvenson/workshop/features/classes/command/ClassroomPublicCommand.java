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
import com.pietersvenson.workshop.command.common.LambdaCommandNode;
import com.pietersvenson.workshop.command.common.Parameter;
import com.pietersvenson.workshop.command.common.ParameterSuppliers;
import com.pietersvenson.workshop.features.classes.Classroom;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

public class ClassroomPublicCommand extends LambdaCommandNode {

  public ClassroomPublicCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.STAFF, "Allow or disallow anyone to join a class", "public",
        (sender, args) -> {
          Optional<Classroom> classroom;
          if (args.length == 0) {
            classroom = Workshop.getInstance().getState().getClassroomManager().getInSession();
            if (!classroom.isPresent()) {
              sender.sendMessage(Format.error("There's no class currently in session!"));
              return false;
            }
          } else {
            classroom = Workshop.getInstance().getState().getClassroomManager().getClassroom(args[0]);
            if (!classroom.isPresent()) {
              sender.sendMessage(Format.error("There's no class with that id!"));
              return false;
            }
          }
          classroom.get().setPublic(!classroom.get().isPublic());
          sender.sendMessage(Format.success(classroom.get().getId()
              + " is now "
              + (classroom.get().isPublic() ? "public" : "private")));
          return true;
        });
    addSubcommand(Parameter.basic(""), "Make the current class public or not");
    addSubcommand(Parameter.builder().supplier(ParameterSuppliers.CLASS_ID).build(), "Use the id of a class");
  }

}
