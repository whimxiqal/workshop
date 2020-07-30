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

package com.pietersvenson.workshop.state;

import com.google.common.io.Files;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.ConfigManager;
import com.pietersvenson.workshop.features.classes.ClassroomManager;
import com.pietersvenson.workshop.features.freeze.FreezeManager;
import com.pietersvenson.workshop.features.home.HomeManager;
import com.pietersvenson.workshop.features.nickname.NicknameManager;
import com.pietersvenson.workshop.features.banitem.BanitemManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.pietersvenson.workshop.features.spawn.SpawnManager;
import com.pietersvenson.workshop.features.tectonic.TectonicManager;
import com.pietersvenson.workshop.features.teleport.TeleportManager;
import com.pietersvenson.workshop.inventory.InventoryMenuManager;
import lombok.Getter;

public final class WorkshopState {

  @Getter
  private final ConfigManager configManager = new ConfigManager();
  @Getter
  private final FreezeManager freezeManager = new FreezeManager();
  @Getter
  private final HomeManager homeManager = new HomeManager();
  @Getter
  private final BanitemManager banitemManager = new BanitemManager();
  @Getter
  private final ClassroomManager classroomManager = new ClassroomManager();
  @Getter
  private final NicknameManager nicknameManager = NicknameManager.getImplemented();
  @Getter
  private final SpawnManager spawnManager = new SpawnManager();
  @Getter
  private final TeleportManager teleportManager = new TeleportManager();
  @Getter
  private final TectonicManager tectonicManager = new TectonicManager();
  @Getter
  private final InventoryMenuManager inventoryMenuManager = new InventoryMenuManager();

  private List<Stateful> getStatefuls() {
    return Arrays.stream(WorkshopState.class.getDeclaredFields())
        .map(field -> {
          try {
            return field.get(this);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(object -> object instanceof Stateful)
        .map(object -> ((Stateful) object))
        .collect(Collectors.toList());
  }

  /**
   * Save the plugin state to be used across restarts.
   */
  public void save() {
    getStatefuls().forEach(this::save);
  }

  public void save(Stateful stateful) {
    try {
      getStateFile(stateful).createNewFile();
      Files.write(stateful.dumpState().getBytes(), getStateFile(stateful));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Load the plugin state from storage.
   */
  public boolean load() {
    AtomicBoolean successful = new AtomicBoolean(true);
    getStatefuls().forEach(stateful -> {
      File stateFile = getStateFile(stateful);
      try {
        if (stateFile.exists()) {
          stateful.loadState(Files.toString(stateFile, StandardCharsets.UTF_8));
        } else {
          save(stateful);
        }
      } catch (Exception e) {
        Workshop.getInstance().getLogger().severe(
            "An error occurred trying to load state data from file: "
                + stateFile.getAbsolutePath());
        e.printStackTrace();
        successful.set(false);
      }
    });
    return successful.get();
  }

  /**
   * Get the state file associated with a specific {@link Stateful}.
   *
   * @param stateful the stateful object
   * @return the appropriate file
   */
  public static File getStateFile(Stateful stateful) {
    Workshop.getInstance().getDataFolder().mkdirs();
    return new File(Workshop.getInstance().getDataFolder().getPath()
        + "/"
        + stateful.getFileName());
  }

}
