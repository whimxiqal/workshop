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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pietersvenson.workshop.config.NullableSettingEnablee;
import com.pietersvenson.workshop.config.Setting;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class CommandNode extends NullableSettingEnablee implements CommandExecutor, TabCompleter {

  public static final int ARG_MAX_LENGTH = 20;

  private final CommandNode parent;
  private final Permission permission;
  private final String description;
  private final List<String> aliases = Lists.newLinkedList();
  private final List<CommandNode> children = Lists.newLinkedList();
  private final Map<Parameter, String> parameters = Maps.newLinkedHashMap();

  /**
   * Simple constructor.
   *
   * @param parent       parent node, null if none
   * @param permission   permission allowing this command
   * @param description  describes the function of this command
   * @param primaryAlias the primary label used to call this command
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @Nonnull String description,
                     @Nonnull String primaryAlias) {
    this(parent, permission, description, primaryAlias, true, null);

  }

  /**
   * Full constructor.
   *
   * @param parent         parent node, null if none
   * @param permission     permission allowing this command
   * @param description    describes the function of this command
   * @param primaryAlias   the primary label used to call this command
   * @param addHelp        whether a help sub-command is generated
   * @param enablerSetting a setting that represents whether this command is enabled, null if none
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @Nonnull String description,
                     @Nonnull String primaryAlias,
                     boolean addHelp,
                     Setting<Boolean> enablerSetting) {
    super(enablerSetting);
    Objects.requireNonNull(description);
    Objects.requireNonNull(primaryAlias);
    this.parent = parent;
    this.permission = permission;
    this.description = description;
    this.aliases.add(primaryAlias);
    if (addHelp) {
      this.children.add(new HelpCommandNode(this));
    }
    setEnabler(enablerSetting);
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

  @Nonnull
  public final String getParameterDescription(@Nonnull Parameter parameter) {
    return parameters.get(parameter);
  }

  protected final void addSubcommand(@Nonnull Parameter parameter, @Nonnull String description) {
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

  protected final void addChildren(CommandNode... nodes) {
    this.children.addAll(Arrays.asList(nodes));
    Set<String> childrenAliases = Sets.newHashSet();
    for (CommandNode child : children) {
      for (String alias : child.getAliases()) {
        if (childrenAliases.contains(alias.toLowerCase())) {
          throw new IllegalStateException("There may not be two of the same alias in the children of a Command Node: "
              + alias);
        }
      }
    }
  }

  @Nonnull
  public final List<CommandNode> getChildren() {
    return children;
  }

  public final boolean isRoot() {
    return parent == null;
  }

  @Override
  public void setEnabler(@Nullable Setting<Boolean> enablerSetting) {
    super.setEnabler(enablerSetting);
    if (enablerSetting != null) {
      addChildren(new EnablerCommandNode(this, enablerSetting));
    }
  }

  public final void sendCommandError(CommandSender sender, String error) {
    sender.sendMessage(Format.error(error));
    sender.sendMessage(Format.error("Try: " + ChatColor.GRAY + "/" + getFullCommand() + " help"));
  }

  public final void sendCommandError(CommandSender sender, CommandError error) {
    sendCommandError(sender, error.getMessage());
  }

  @Override
  public final boolean onCommand(@Nonnull CommandSender sender,
                                 @Nonnull Command command,
                                 @Nonnull String label,
                                 @Nonnull String[] args) {

    // Adds support for quotations around space-delimited arguments
    String[] actualArgs;
    if (isRoot()) {
      actualArgs = Format.combineQuotedArguments(args);
      for (String arg : actualArgs) {
        if (arg.length() > ARG_MAX_LENGTH) {
          sender.sendMessage(Format.error("Arguments cannot exceed "
              + ARG_MAX_LENGTH
              + " characters!"));
          return false;
        }
      }
    } else {
      actualArgs = Arrays.copyOf(args, args.length);
    }

    // Main method
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      sender.sendMessage(Format.error("You don't have permission to do this!"));
      return false;
    }
    if (actualArgs.length < 1) {
      return onWrappedCommand(sender, command, label, actualArgs);
    }
    for (CommandNode child : children) {
      for (String alias : child.aliases) {
        if (alias.equalsIgnoreCase(actualArgs[0])) {
          if (isEnabled() || child instanceof EnablerCommandNode) {
            return child.onCommand(sender, command, child.getPrimaryAlias(), Arrays.copyOfRange(actualArgs, 1, actualArgs.length));
          }
        }
      }
    }
    if (!isEnabled()) {
      sender.sendMessage(Format.error("This command is not enabled"));
      return false;
    }
    return onWrappedCommand(sender, command, label, actualArgs);
  }

  public abstract boolean onWrappedCommand(@Nonnull CommandSender sender,
                                           @Nonnull Command command,
                                           @Nonnull String label,
                                           @Nonnull String[] args);

  @Override
  public final List<String> onTabComplete(@Nonnull CommandSender sender,
                                          @Nonnull Command command,
                                          @Nonnull String label,
                                          @Nonnull String[] args) {
    List<String> cmds = Lists.newLinkedList();
    if (!isEnabled()) {
      if (sender.hasPermission(Permissions.STAFF)) {
        cmds.add("enable");
      }
      return cmds;
    }
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      return cmds; // empty
    }
    if (args.length == 0) {
      return cmds; // empty
    }
    for (CommandNode child : children) {
      for (int i = 0; i < child.aliases.size(); i++) {
        String alias = child.aliases.get(i);

        // If any alias matches from this child, then bump us up to its children
        if (alias.equalsIgnoreCase(args[0])) {
          return child.onTabComplete(sender,
              command,
              child.getPrimaryAlias(),
              Arrays.copyOfRange(args, 1, args.length));
        }

        // Only if we're on the last arg of the recursion and we're at the primary alias,
        // and we have permission to the command, add it
        if (args.length == 1 && i == 0) {
          if (child.permission == null || sender.hasPermission(child.permission)) {
            cmds.add(alias);
          }
        }
      }
    }

    for (Parameter param : parameters.keySet()) {
      cmds.addAll(param.nextAllowedInputs(sender, Arrays.copyOfRange(args, 0, args.length - 1)));
    }

    List<String> out = Lists.newLinkedList();
    StringUtil.copyPartialMatches(args[args.length - 1], cmds, out);
    Collections.sort(out);
    return out;
  }

}
