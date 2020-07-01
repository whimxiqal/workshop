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

package com.pietersvenson.workshop.features.home;

import com.google.common.collect.Maps;
import com.pietersvenson.workshop.state.Stateful;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class HomeManager implements Stateful {

  public static final String BASIC_FILE_NAME = "homes";

  Map<UUID, Location> homes = Maps.newHashMap();

  public Optional<Location> getHome(@Nonnull final Player player) {
    return Optional.ofNullable(homes.get(player.getUniqueId()));
  }

  public Location putHome(@Nonnull final Player player, @Nonnull final Location location) {
    return homes.put(player.getUniqueId(), location);
  }

  public Map<String, Location> getHomesByName() {
    return this.homes.entrySet()
        .stream()
        .collect(Collectors.toMap(entry ->
            Bukkit.getOfflinePlayer(entry.getKey()).getName(), Map.Entry::getValue));
  }

  @Override
  public String getBasicFileName() {
    return "homes";
  }

  @Override
  public String getState() {
    Map<String, Map<String, Object>> serializedState = new TreeMap<>();
    homes.forEach((uuid, location) -> {
      serializedState.put(uuid.toString(), location.serialize());
    });
    // TODO implement correctly
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setIndent(2);
    dumperOptions.setPrettyFlow(true);
    return new Yaml(dumperOptions).dump(serializedState);
  }

  @Override
  public void loadStateful(String state) {
    homes.clear();
    new Yaml().<Map<String, Map<String, Object>>>load(state).forEach(
        (uuid, location) ->
            homes.put(UUID.fromString(uuid), Location.deserialize(location)));
  }
}
