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

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandTree {

  private CommandNode root;

  public CommandTree(@Nonnull CommandNode root) {
    this.root = Preconditions.checkNotNull(root);
  }

  public CommandNode root() {
    return root;
  }

  public static void register(@Nonnull JavaPlugin plugin, @Nonnull CommandNode root) {
    Preconditions.checkNotNull(plugin.getCommand(root.getPrimaryAlias())).setExecutor(root);
  }

  public abstract static class CommandNode implements CommandExecutor {
    private final CommandNode parent;
    private final Permission permission;
    private final String description;
    private final List<String> aliases = new ArrayList<>();
    private final List<CommandNode> children = new ArrayList<>();
    private final String parameterUsage;

    public CommandNode(@Nullable CommandNode parent,
                       @Nullable Permission permission,
                       @Nonnull String description,
                       @Nonnull String primaryAlias,
                       @Nullable List<String> otherAliases,
                       @Nonnull String parameterUsage) {
      this(parent, permission, description, primaryAlias, otherAliases, parameterUsage, true);

    }

    public CommandNode(@Nullable CommandNode parent,
                       @Nullable Permission permission,
                       @Nonnull String description,
                       @Nonnull String primaryAlias,
                       @Nullable List<String> otherAliases,
                       @Nonnull String parameterUsage,
                       boolean addHelp) {
      Workshop.getInstance().getLogger().info("Creating a new CommandNode: " + primaryAlias);
      Preconditions.checkNotNull(description);
      Preconditions.checkNotNull(primaryAlias);
      Preconditions.checkNotNull(parameterUsage);
      this.parent = parent;
      this.permission = permission;
      this.description = description;
      this.aliases.add(primaryAlias);
      if (otherAliases != null) {
        this.aliases.addAll(otherAliases);
      }
      this.parameterUsage = parameterUsage;
      if (addHelp) {
        this.children.add(new HelpCommandNode(this));
      }
    }

    // Getters and Setters

    @Nullable
    public CommandNode getParent() {
      return parent;
    }

    @Nonnull
    public Optional<Permission> getPermission() {
      return Optional.ofNullable(permission);
    }

    @Nonnull
    public String getDescription() {
      return description;
    }

    @Nonnull
    public String getPrimaryAlias() {
      return aliases.get(0);
    }

    @Nonnull
    public List<String> getAliases() {
      return aliases;
    }

    public void addAliases(String... aliases) {
      this.aliases.addAll(Arrays.asList(aliases));
    }

    @Nonnull
    public String getUsage() {
      StringBuilder command = new StringBuilder(getPrimaryAlias());
      CommandNode cur = this;
      while (!cur.isRoot()) {
        command.insert(0, cur.parent.getPrimaryAlias() + " ");
        cur = cur.parent;
      }
      return command.toString() + " " + getParameterUsage();
    }

    public void addChildren(CommandNode... nodes) {
      this.children.addAll(Arrays.asList(nodes));
    }

    @Nonnull
    public List<CommandNode> getChildren() {
      return children;
    }

    @Nonnull
    public String getParameterUsage() {
      return parameterUsage;
    }

    public boolean isRoot() {
      return parent == null;
    }

    public void sendCommandError(CommandSender sender, String error) {
      sender.sendMessage(Format.error(error + "\nUsage: " + ChatColor.GRAY + "/" + getUsage()));
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 1) {
        return onWrappedCommand(sender, command, label, args);
      }
      for (CommandNode child : children) {
        for (String alias : child.aliases) {
          if (alias.equalsIgnoreCase(args[0])) {
            return child.onCommand(sender, command, child.getPrimaryAlias(), Arrays.copyOfRange(args, 1, args.length));
          }
        }
      }
      return onWrappedCommand(sender, command, label, args);
    }

    public abstract boolean onWrappedCommand(CommandSender sender, Command command, String label, String[] args);

  }

  private static final class HelpCommandNode extends CommandNode implements CommandExecutor {

    public HelpCommandNode(@Nonnull CommandNode parent) {
      super(parent,
          null,
          "Command help for " + parent.getUsage(),
          "help",
          null,
          "",
          false);
      addAliases("?");
      Preconditions.checkNotNull(parent);
    }

    @Override
    public boolean onWrappedCommand(CommandSender sender, Command command, String label, String[] args) {
      sender.sendMessage(Format.info(getDescription()));
      // TODO: format help response
      return true;
    }

  }

}

