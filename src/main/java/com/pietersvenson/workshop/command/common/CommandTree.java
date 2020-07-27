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

import java.util.Objects;
import javax.annotation.Nonnull;

import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandTree {

  private CommandNode root;

  public CommandTree(@Nonnull CommandNode root) {
    this.root = Objects.requireNonNull(root);
  }

  public CommandNode root() {
    return root;
  }

  /**
   * Register all functionality of a command, noted in the plugin.yml.
   *
   * @param plugin the plugin under which this command is registered
   * @param root   the root CommandNode of the entire command tree,
   *               whose name is registered in the plugin.yml
   * @return the tree of all commands with the given root
   */
  public static CommandTree register(@Nonnull JavaPlugin plugin, @Nonnull CommandNode root) {
    CommandTree tree = new CommandTree(root);
    PluginCommand command = Objects.requireNonNull(plugin.getCommand(root.getPrimaryAlias()));
    command.setExecutor(root);
    command.setTabCompleter(root);
    root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    return tree;
  }

}

