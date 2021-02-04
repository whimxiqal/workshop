///*
// * MIT License
// *
// * Copyright (c) 2020 Pieter Svenson
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// *
// */
//
//package com.pietersvenson.workshop.features.nickname;
//
//import com.pietersvenson.workshop.features.FeatureListener;
//import org.inventivetalent.nicknamer.api.NickNamerAPI;
//
//import javax.annotation.Nonnull;
//import java.util.Collection;
//import java.util.Optional;
//import java.util.UUID;
//
//public class NicknamerNicknameManager extends NicknameManager {
//
//  @Override
//  boolean hasNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
//    return NickNamerAPI.getNickManager().isNicked(playerUuid);
//  }
//
//  @Override
//  boolean isNicknameUsed(@Nonnull String nick) throws UnsupportedOperationException {
//    return NickNamerAPI.getNickManager().isNickUsed(nick);
//  }
//
//  @Nonnull
//  @Override
//  public Optional<String> getNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
//    return Optional.ofNullable(NickNamerAPI.getNickManager().getNick(playerUuid));
//  }
//
//  @Override
//  public void setNickname(@Nonnull UUID playerUuid, @Nonnull String nick) throws UnsupportedOperationException {
//    NickNamerAPI.getNickManager().setNick(playerUuid, nick);
//  }
//
//  @Override
//  void removeNickname(@Nonnull UUID playerUuid) throws UnsupportedOperationException {
//    NickNamerAPI.getNickManager().removeNick(playerUuid);
//  }
//
//  @Nonnull
//  @Override
//  protected Collection<FeatureListener> getListeners() {
//    return super.getListeners();
//  }
//}
