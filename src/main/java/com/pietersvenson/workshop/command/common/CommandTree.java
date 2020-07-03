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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.StringUtil;

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
    Preconditions.checkNotNull(plugin.getCommand(root.getPrimaryAlias())).setTabCompleter(root);
  }

  public abstract static class CommandNode implements CommandExecutor, TabCompleter {
    private final CommandNode parent;
    private final Permission permission;
    private final String description;
    private final List<String> aliases = Lists.newLinkedList();
    private final List<CommandNode> children = Lists.newLinkedList();
    private final Map<Parameter, String> parameters = Maps.newLinkedHashMap();

    public CommandNode(@Nullable CommandNode parent,
                       @Nullable Permission permission,
                       @Nonnull String description,
                       @Nonnull String primaryAlias) {
      this(parent, permission, description, primaryAlias, true);

    }

    public CommandNode(@Nullable CommandNode parent,
                       @Nullable Permission permission,
                       @Nonnull String description,
                       @Nonnull String primaryAlias,
                       boolean addHelp) {
      Preconditions.checkNotNull(description);
      Preconditions.checkNotNull(primaryAlias);
      this.parent = parent;
      this.permission = permission;
      this.description = description;
      this.aliases.add(primaryAlias);
      if (addHelp) {
        this.children.add(new HelpCommandNode(this));
      }
    }

    // Getters and Setters

    @Nullable
    public final CommandNode getParent() {
      return parent;
    }

    @Nonnull
    public final Optional<Permission> getPermission() {
      return Optional.ofNullable(permission);
    }

    @Nonnull
    public final String getDescription() {
      return description;
    }

    @Nonnull
    public final String getPrimaryAlias() {
      return aliases.get(0);
    }

    @Nonnull
    public final List<String> getAliases() {
      return aliases;
    }

    public final void addAliases(@Nonnull String... aliases) {
      this.aliases.addAll(Arrays.asList(aliases));
    }

    @Nonnull
    public final List<Parameter> getParameters() {
      return Lists.newLinkedList(parameters.keySet());
    }

    public final void addInput(@Nonnull Parameter parameter, @Nonnull String description) {
      this.parameters.put(parameter, description);
    }

    @Nonnull
    public final String getFullCommand() {
      StringBuilder command = new StringBuilder(getPrimaryAlias());
      CommandNode cur = this;
      while (!cur.isRoot()) {
        command.insert(0, cur.parent.getPrimaryAlias() + " ");
        cur = cur.parent;
      }
      return command.toString();
    }

    public final void addChildren(CommandNode... nodes) {
      this.children.addAll(Arrays.asList(nodes));
    }

    @Nonnull
    public final List<CommandNode> getChildren() {
      return children;
    }

    // TODO implement Parameter

    public final boolean isRoot() {
      return parent == null;
    }

    public final void sendCommandError(CommandSender sender, String error) {
      sender.sendMessage(Format.error(error + "\nTry: " + ChatColor.GRAY + "/" + getFullCommand() + " help"));
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (this.permission != null && !sender.hasPermission(this.permission)) {
        sender.sendMessage(Format.error("You don't have permission to do this!"));
        return false;
      }
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

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
      List<String> cmds = Lists.newLinkedList();
      if (this.permission != null && !sender.hasPermission(this.permission)) {
        return cmds; // empty
      }
      if (args.length == 0) {
        return cmds; // empty
      }
      for (CommandNode child : children) {
        for (int i = 0; i < child.aliases.size(); i++) {
          String alias = child.aliases.get(i);
          if (alias.equalsIgnoreCase(args[0])) {
            return child.onTabComplete(sender, command, child.getPrimaryAlias(), Arrays.copyOfRange(args, 1, args.length));
          }
          if (args.length == 1 && i == 0 && (child.permission == null || sender.hasPermission(child.permission))) {
            cmds.add(alias);
          }
        }
      }
      for (Parameter param : parameters.keySet()) {
        cmds.addAll(param.getNextEntries(sender, Arrays.copyOfRange(args, 0, args.length - 1)));
      }
      List<String> out = Lists.newLinkedList();
      StringUtil.copyPartialMatches(args[args.length - 1], cmds, out);
      Collections.sort(out);
      return out;
    }

  }

  private static final class HelpCommandNode extends CommandNode {

    public HelpCommandNode(@Nonnull CommandNode parent) {
      super(parent,
          null,
          "Get help for this command",
          "help",
          false);
      Preconditions.checkNotNull(parent);
      addAliases("?");
    }

    @Override
    public boolean onWrappedCommand(CommandSender sender, Command command, String label, String[] args) {
      CommandNode parent = Preconditions.checkNotNull(this.getParent());
      sender.sendMessage(Format.success("Command Help: " + ChatColor.GRAY + parent.getFullCommand()));
      for (CommandNode node : parent.getChildren()) {
        if (node.getPermission().map(sender::hasPermission).orElse(true)) {
          StringBuilder builder = new StringBuilder();
          builder.append(ChatColor.GRAY)
              .append("> ")
              .append(parent.getPrimaryAlias())
              .append(" ")
              .append(ChatColor.AQUA)
              .append(node.getPrimaryAlias());
          if (node.getChildren().size() > 1 || !node.getParameters().isEmpty()) {
            builder.append(" < . . . >");
          }
          builder.append(ChatColor.GRAY)
              .append(": ")
              .append(ChatColor.WHITE)
              .append(node.getDescription());
          sender.sendMessage(builder.toString());
        }
      }
      for (Parameter parameter : parent.getParameters()) {
        if (parameter.getPermission().filter(sender::hasPermission).isPresent()) {
          parameter.getFullUsage(sender).ifPresent(usage -> {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.GRAY)
                .append("> ")
                .append(parent.getPrimaryAlias())
                .append(" ")
                .append(ChatColor.AQUA)
                .append(usage)
                .append(ChatColor.GRAY)
                .append(" > ")
                .append(ChatColor.WHITE)
                .append(parent.parameters.get(parameter));
            sender.sendMessage(builder.toString());
          });
        }
      }
      return true;

    }

  }

}

