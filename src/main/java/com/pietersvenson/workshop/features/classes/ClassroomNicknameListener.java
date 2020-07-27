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

package com.pietersvenson.workshop.features.classes;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.FeatureEventHandler;
import com.pietersvenson.workshop.features.FeatureListener;
import com.pietersvenson.workshop.permission.Permissions;
import com.pietersvenson.workshop.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Optional;

public class ClassroomNicknameListener extends FeatureListener {

  ClassroomNicknameListener() {
    super(Settings.ENABLE_NICKNAME_PARTICIPANTS);
  }

  @FeatureEventHandler
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    Player player = playerJoinEvent.getPlayer();
    List<Classroom> progressing = Workshop.getInstance().getState().getClassroomManager().getInSession();

    if (!player.hasPermission(Permissions.STAFF)) {
      // Ensure they are allowed in the server
      if (Workshop.getInstance().getState().getClassroomManager().hasAny()) {
        Optional<Classroom.Participant> participant = progressing.stream()
            .flatMap(room -> room.getParticipants().stream())
            .filter(part -> part.getPlayerUuid().equals(player.getUniqueId()))
            .findFirst();

        if (!participant.isPresent()) {
          return;
        }

        try {
          Workshop.getInstance()
              .getState()
              .getNicknameManager()
              .setNickname(participant.get().getPlayerUuid(),
                  Format.participantDisplayName(participant.get(), player.getName()));
        } catch (UnsupportedOperationException e) {
          Bukkit.getServer().broadcastMessage(Format.info(player.getName() + " is "
              + ChatColor.GREEN + participant.get().getFirstName() + " "
              + participant.get().getLastName().substring(0, 1) + "."));
        }
      }
    }
  }


}

