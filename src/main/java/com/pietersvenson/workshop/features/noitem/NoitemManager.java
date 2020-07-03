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
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.state.Stateful;
import com.pietersvenson.workshop.util.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NoitemManager implements Stateful {

  Set<String> banned = Sets.newHashSet();

  public boolean ban(Material item) {
    Bukkit.getOnlinePlayers()
        .stream()
        .filter(player -> !player.hasPermission(Permissions.STAFF))
        .forEach(player -> Inventories.clearBannedItems(player.getInventory()));
    if (!item.isItem()) {
      return false;
    }
    return this.banned.add(item.toString().toUpperCase());
  }

  public boolean unban(Material item) {
    return this.banned.remove(item.toString().toUpperCase());
  }

  public boolean isBanned(Material item) {
    return this.banned.contains(item.toString().toUpperCase());
  }

  public List<Material> getBanned() {
    return banned.stream()
        .map(Material::matchMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
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
}
