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

import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.features.FeatureManager;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class NicknameManager extends FeatureManager {

  public static NicknameManager getImplemented() {
    if (Bukkit.getPluginManager().getPlugin("NickNamer") != null) {
      return new NicknameManager();
//      return new NicknamerNicknameManager();
    } else if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
      return new EssentialsNicknameManager();
    } else {
      return new NicknameManager();
    }
  }

  boolean hasNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This operation is not supported by the NicknameManager");
  }

  boolean isNicknameUsed(@Nonnull String nick) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This operation is not supported by the NicknameManager");
  }

  @Nonnull
  public Optional<String> getNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This operation is not supported by the NicknameManager");
  }

  public void setNickname(@Nonnull UUID playerUuid, @Nonnull String nick) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This operation is not supported by the NicknameManager");
  }

  void removeNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This operation is not supported by the NicknameManager");
  }

  @Nonnull
  @Override
  protected Collection<FeatureListener> getListeners() {
    return Collections.emptyList();
  }
}
