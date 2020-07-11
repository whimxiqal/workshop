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

package com.pietersvenson.workshop.features.noitem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.state.Stateful;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class NoitemManager extends FeatureManager implements Stateful {

  Set<String> banned = Sets.newHashSet();

  /**
   * Add an item to the ban set.
   *
   * @param item the item to ban
   * @return true if addition was successful
   */
  public boolean ban(Material item) {
    if (Settings.ENABLE_NOITEM.getValue()) {
      Bukkit.getOnlinePlayers()
          .stream()
          .filter(player -> !player.hasPermission(Permissions.STAFF))
          .forEach(player -> scheduledClean(player.getInventory()));
    }
    if (!item.isItem()) {
      return false;
    }
    return this.banned.add(item.toString().toUpperCase());
  }

  /**
   * Remove an item from the ban set.
   *
   * @param item the item to unban
   * @return true if removal was successful
   */
  public boolean unban(Material item) {
    return this.banned.remove(item.toString().toUpperCase());
  }

  public boolean isBanned(Material item) {
    return this.banned.contains(item.toString().toUpperCase());
  }

  /**
   * Get all banned items in a list.
   *
   * @return the banned materials
   */
  public List<Material> getBanned() {
    return banned.stream()
        .map(Material::matchMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Schedule a clean of a given inventory, which removes all banned items.
   *
   * @param inventory the inventory to clean
   */
  public void scheduledClean(Inventory inventory) {
    Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> {
      for (int i = 0; i < inventory.getSize(); i++) {
        ItemStack itemStack = inventory.getItem(i);
        if (itemStack != null && isBanned(itemStack.getType())) {
          inventory.clear(i);
        }
      }
    });
  }

  @Nonnull
  @Override
  public String getFileName() {
    return "noitem.yml";
  }

  @Nonnull
  @Override
  public String dumpState() {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setIndent(2);
    dumperOptions.setPrettyFlow(true);
    return new Yaml(dumperOptions).dump(banned.toArray());
  }

  @Override
  public void loadState(String state) throws YAMLException {
    List<String> toAdd = Lists.newLinkedList();
    try {
      toAdd.addAll(new Yaml().<List<String>>load(state));
    } catch (Exception e) {
      throw new YAMLException(e);
    }
    banned.clear();
    toAdd.forEach(name -> {
      Material material = Material.matchMaterial(name);
      if (material == null || !ban(material)) {
        Workshop.getInstance().getLogger().warning(
            "An invalid or duplicate banned item was found: "
                + name
                + ". This has been skipped.");
      }
    });
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Collections.singleton(new NoitemListener());
  }

}
