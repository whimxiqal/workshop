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
import com.pietersvenson.workshop.util.Communication;
import com.pietersvenson.workshop.util.Format;
import com.pietersvenson.workshop.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class ClassroomListener extends FeatureListener {

  protected ClassroomListener() {
    super(Settings.ENABLE_CLASSES);
  }

  @FeatureEventHandler
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    Player player = playerJoinEvent.getPlayer();
    Optional<Classroom> progressing = Workshop.getInstance().getState().getClassroomManager().getInSession();

    if (!player.hasPermission(Permissions.STAFF)) {

      // Ensure they are allowed in the server
      if (Workshop.getInstance().getState().getClassroomManager().hasAny()) {
        if (!progressing.isPresent()) {
          player.kickPlayer(
              "There are no classes currently in progress!");
          return;
        } else {
          // Get first one. There shouldn't be any more than that.
          Optional<Classroom.Participant> participant = progressing.get().getParticipants().stream().filter(part -> part.getPlayerUuid().equals(player.getUniqueId())).findAny();
          if (participant.isPresent()) {
            // Welcome them to the class!
            player.sendMessage(Format.info("Welcome to "
                + ChatColor.LIGHT_PURPLE + progressing.get().getName()
                + Format.INFO + "!"));
            if (participant.get().giveNickname()) {
              player.sendMessage(Format.info(
                  "Your name is set to "
                      + Workshop.getInstance().getState().getNicknameManager().getNickname(participant.get().getPlayerUuid())));
            }
          } else if (progressing.get().isPublic()) {
            Optional<RegistrationForm> form = progressing.get().getRegistrationForm(player.getUniqueId());
            if (!form.isPresent()) {
              form = Optional.of(progressing.get().startRegistering(player.getUniqueId()));
            }
            notify(player, form.get());
          } else {
            player.kickPlayer("You're not registered for this class: \n"
                + progressing.get().getName() + "\n\n"
                + "Please contact info@einsteinsworkshop.com if you think this is an error.");
            Communication.sendStaffMessage(Format.warn("The player "
                + player.getName()
                + " just tried to log in but is not registered for a class in session"));
          }
        }
      }
    }
  }

  @FeatureEventHandler
  public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
    if (!playerMoveEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      if (!Workshop.getInstance().getState().getClassroomManager().getInSession().isPresent()) {
        return;
      }
      if (!Workshop.getInstance().getState().getClassroomManager().getInSession().get().isPublic()) {
        return;
      }
      Optional<RegistrationForm> form = Workshop.getInstance()
          .getState()
          .getClassroomManager()
          .getInSession().flatMap(classroom -> classroom.getRegistrationForm(playerMoveEvent.getPlayer().getUniqueId()));
      if (form.isPresent()) {
        playerMoveEvent.setCancelled(true);
        Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> notify(playerMoveEvent.getPlayer(), form.get()));
      }
    }
  }

  @FeatureEventHandler
  public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
    if (!playerInteractEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      if (Workshop.getInstance().getState().getClassroomManager().getInSession().map(classroom -> classroom.isRegistering(playerInteractEvent.getPlayer().getUniqueId())).orElse(false)) {
        playerInteractEvent.setCancelled(true);
      }
    }
  }

  @FeatureEventHandler
  public void onPlayerChat(AsyncPlayerChatEvent playerChatEvent) {
    if (!playerChatEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      Optional<RegistrationForm> form = Workshop.getInstance()
          .getState()
          .getClassroomManager()
          .getInSession().flatMap(classroom -> classroom.getRegistrationForm(playerChatEvent.getPlayer().getUniqueId()));
      if (form.isPresent()) {
        String message = playerChatEvent.getMessage();
        if (!Validate.isName(message)) {
          playerChatEvent.getPlayer().sendMessage(Format.error("I'm sorry, that contains invalid characters"));
          return;
        }
        form.get().input(message);
        playerChatEvent.setCancelled(true);
        Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> {
          if (form.get().isDone()) {
            Optional<Classroom.Participant> participant = Workshop.getInstance().getState()
                .getClassroomManager()
                .getInSession().get()
                .completeRegistration(playerChatEvent.getPlayer().getUniqueId());
            if (!participant.isPresent()) {
              Workshop.getInstance().getLogger().warning("An error occurred trying to register " + playerChatEvent.getPlayer());
              return;
            }
            unnotify(playerChatEvent.getPlayer());
            playerChatEvent.getPlayer().sendMessage(Format.success("Thank you for registering!"));
            Bukkit.getServer().broadcastMessage(Format.info(
                participant.get().getFirstName() + " " + participant.get().getLastName()
                + " (" + Format.ACCENT_1 + playerChatEvent.getPlayer().getName() + Format.INFO + ")"
                + " just registered for the class "
                + Workshop.getInstance().getState().getClassroomManager().getInSession().get().getId()));
            if (participant.get().giveNickname()) {
              playerChatEvent.getPlayer().sendMessage(Format.info(
                  "Your name is set to "
                      + Workshop.getInstance().getState().getNicknameManager().getNickname(participant.get().getPlayerUuid())));
            }
          } else {
            notify(playerChatEvent.getPlayer(), form.get());
          }
        });
      }
    }
  }

  @FeatureEventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent playerCommandEvent) {
    if (!playerCommandEvent.getPlayer().hasPermission(Permissions.STAFF)) {
      if (Workshop.getInstance().getState().getClassroomManager().getInSession().map(classroom -> classroom.isRegistering(playerCommandEvent.getPlayer().getUniqueId())).orElse(false)) {
        playerCommandEvent.setCancelled(true);
        playerCommandEvent.getPlayer().sendMessage(Format.error("You can't run commands when you are registering!"));
      }
    }
  }

  private void notify(Player player, RegistrationForm form) {
    player.setInvulnerable(true);
    Location location = player.getLocation();
    location.setPitch(-90);
    player.teleport(location);
    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000, 10));
    player.resetTitle();
    player.sendTitle(Format.WARN + form.message(), "Press t to chat", 10, 10000, 0);
  }

  private void unnotify(Player player) {
    player.setInvulnerable(false);
    Location location = player.getLocation();
    location.setPitch(0);
    player.teleport(location);
    player.removePotionEffect(PotionEffectType.BLINDNESS);
    player.resetTitle();
  }

}
