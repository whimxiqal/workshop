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

package com.pietersvenson.workshop.command.common;

import com.pietersvenson.workshop.config.Setting;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnablerCommandNode extends LambdaCommandNode {

  public EnablerCommandNode(@Nonnull CommandNode parent, @Nonnull Setting<Boolean> enablerSetting) {
    super(parent,
        Permissions.STAFF,
        "Enables or disables the feature associated with the "
            + Format.INFO + parent.getFullCommand()
            + Format.ERROR + " command",
        "enable",
        (sender, args) -> {
          enablerSetting.setValue(!enablerSetting.getValue());
          sender.sendMessage(Format.success("The feature associated with the "
              + Format.INFO + parent.getFullCommand()
              + Format.SUCCESS + " command has been "
              + Format.WARN + (enablerSetting.getValue() ? "enabled" : "disabled")));
          return true;
        });
  }

}
