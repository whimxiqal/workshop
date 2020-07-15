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

package com.pietersvenson.workshop.features.spawn;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import com.pietersvenson.workshop.state.Stateful;
import org.bukkit.Location;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class SpawnManager extends FeatureManager implements Stateful {

  private Map<String, Object> spawn;

  public void setSpawn(@Nonnull Location location) {
    this.spawn = location.serialize();
    Workshop.getInstance().saveStateSynchronous();
  }

  public Optional<Location> getSpawn() {
    return Optional.ofNullable(spawn).map(Location::deserialize);
  }

  @Nonnull
  @Override
  public String getFileName() {
    return "spawn.yml";
  }

  @Nonnull
  @Override
  public String dumpState() {
    Map<String, Map<String, Object>> serializedState = new TreeMap<>();
    serializedState.put("spawn", spawn);
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setIndent(2);
    dumperOptions.setPrettyFlow(true);
    return new Yaml(dumperOptions).dump(serializedState);
  }

  @Override
  public void loadState(String state) throws YAMLException {
    spawn = null;
    try {
      Map<String, Map<String, Object>> loaded = new Yaml().<Map<String, Map<String, Object>>>load(state);
      spawn = loaded.get("spawn");
    } catch (Exception e) {
      throw new YAMLException(e);
    }
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Collections.emptyList();
  }
}
