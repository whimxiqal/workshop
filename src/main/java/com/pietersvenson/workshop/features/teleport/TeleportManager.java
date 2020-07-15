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

package com.pietersvenson.workshop.features.teleport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.DeafFeatureManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TeleportManager extends DeafFeatureManager {

  private Table<UUID, UUID, Instant> requests = HashBasedTable.create();

  public boolean hasRequest(@Nonnull UUID requester, @Nonnull UUID destination) {
    return requests.get(requester, destination) != null;
  }

  public void request(@Nonnull UUID requester, @Nonnull UUID destination) {
    requests.put(Objects.requireNonNull(requester),
        Objects.requireNonNull(destination),
        Instant.now());
  }

  public boolean expired(UUID requester, UUID destination) {
    return Optional.ofNullable(requests.get(requester, destination))
        .map(time -> time.plus(Settings.TELEPORTING_TIMEOUT.getValue(), ChronoUnit.SECONDS).isAfter(Instant.now()))
        .orElse(true);
  }

  public boolean accept(@Nonnull UUID requester, @Nonnull UUID destination) {
    Instant requestTime = requests.get(requester, destination);
    if (requestTime == null) {
      return false;
    }
    if (requestTime.plus(Settings.TELEPORTING_TIMEOUT.getValue(), ChronoUnit.SECONDS).isAfter(Instant.now())) {
      return false;
    }
    Player requesterPlayer = Bukkit.getPlayer(requester);
    Player destinationPlayer = Bukkit.getPlayer(destination);
    if (requesterPlayer == null || destinationPlayer == null) {
      return false;
    }
    requesterPlayer.teleport(destinationPlayer.getLocation());
    return true;
  }

  public boolean deny(@Nonnull UUID requester, @Nonnull UUID destination) {
    Instant requestTime = requests.get(requester, destination);
    if (requestTime == null) {
      return false;
    }
    requests.remove(requester, destination);
    return true;
  }


}
