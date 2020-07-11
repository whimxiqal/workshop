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

package com.pietersvenson.workshop;

import com.pietersvenson.workshop.command.WorkshopCommandRoot;
import com.pietersvenson.workshop.command.common.CommandTree;
import com.pietersvenson.workshop.state.WorkshopState;
import com.pietersvenson.workshop.util.Reference;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "Workshop", version = Reference.VERSION)
@Description(Reference.DESCRIPTION)
@Author("Pieter Svenson")
@Website("www.pietersvenson.com")
@Commands(
    @Command(name = "workshop",
        desc = WorkshopCommandRoot.DESCRIPTION,
        aliases = {"ws"},
        permission = "workshop.command"))
@Permission(name = "workshop.command", desc = "Allows usage of workshop command")
@Permission(name = "workshop.staff", desc = "Allows usage of all staff abilities")
@ApiVersion(ApiVersion.Target.v1_15)
public final class Workshop extends JavaPlugin {

  @Getter
  private static Workshop instance;

  @Getter
  private WorkshopState state;

  @Override
  public void onEnable() {
    Workshop.instance = this;

    state = new WorkshopState();

    getLogger().info("Loading Previous State...");
    getState().load();

    getLogger().info("Registering commands...");
    CommandTree.register(this, new WorkshopCommandRoot());

//    getLogger().info("Registering listeners...");
//    getServer().getPluginManager().registerEvents(new PlayerListener(), this);
//    getServer().getPluginManager().registerEvents(new InventoryListener(), this);



  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Saving Workshop State...");
    getState().save();

    getLogger().info("Goodbye!");
  }

}
