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

package com.pietersvenson.workshop.features.nickname;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.pietersvenson.workshop.Workshop;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EssentialsNicknameManager extends NicknameManager {

  Essentials essentials;

  public EssentialsNicknameManager() {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
    if (!(plugin instanceof Essentials)) {
      throw new UnsupportedOperationException("The essentials plugin could not be found");
    }
    essentials = (Essentials) plugin;
  }


  @Override
  public boolean hasNickname(@Nonnull UUID playerUuid) {
    User user = essentials.getUser(playerUuid);
    return user != null && user.getNickname() != null;
  }

  @Override
  public boolean isNicknameUsed(@Nonnull String nick) {
    for (UUID uuid : essentials.getUserMap().getAllUniqueUsers()) {
      User user = essentials.getUser(uuid);
      if (user.getNickname() != null && user.getNick(false).equals(nick)) {
        return true;
      }
    }
    return false;
  }

  @Nonnull
  @Override
  public Optional<String> getNickname(@Nonnull UUID playerUuid) {
    return Optional.ofNullable(essentials.getUser(playerUuid)).map(u -> u.getNick(false));
  }

  @Override
  public void setNickname(@Nonnull UUID playerUuid, @Nonnull String nick) {
    setNicknameLiteral(playerUuid, Objects.requireNonNull(nick));
  }

  @Override
  public void removeNickname(@Nonnull UUID playerUuid) {
    setNicknameLiteral(playerUuid, null);
  }

  private void setNicknameLiteral(UUID playerUuid, String nick) {
    User user = essentials.getUser(playerUuid);
    NickChangeEvent nickEvent = new NickChangeEvent(null, user, nick);
    Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> {
      try {
        Bukkit.getServer().getPluginManager().callEvent(nickEvent);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
      if (!nickEvent.isCancelled()) {
        user.setNickname(nick);
        user.setDisplayNick();
      }
    });
  }

}
