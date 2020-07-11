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
//package com.pietersvenson.workshop.listener;
//
//import com.pietersvenson.workshop.Workshop;
//import com.pietersvenson.workshop.features.classes.Classroom;
//import com.pietersvenson.workshop.permission.Permissions;
//import com.pietersvenson.workshop.util.Communication;
//import com.pietersvenson.workshop.util.Format;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.AsyncPlayerChatEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.event.player.PlayerJoinEvent;
//import org.bukkit.event.player.PlayerMoveEvent;
//
//import java.util.List;
//import java.util.Optional;
//
//public class PlayerListener implements Listener {
//
//  // TODO move listeners to individual features
//
//  @EventHandler
//  public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
//    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerMoveEvent.getPlayer())) {
//      playerMoveEvent.setCancelled(true);
//    }
//  }
//
//  @EventHandler
//  public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
//    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerInteractEvent.getPlayer())) {
//      playerInteractEvent.setCancelled(true);
//    }
//  }
//
//  @EventHandler
//  public void onPlayerChat(AsyncPlayerChatEvent playerChatEvent) {
//    if (Workshop.getInstance().getState().getFreezeManager().isFrozen(playerChatEvent.getPlayer())) {
//      playerChatEvent.setCancelled(true);
//      playerChatEvent.getPlayer().sendMessage(Format.error("You can't chat when you are frozen!"));
//    }
//  }
//
//  @EventHandler
//  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
//    Player player = playerJoinEvent.getPlayer();
//    List<Classroom> progressing = Workshop.getInstance().getState().getClassroomManager().getInSession();
//
//    if (!player.hasPermission(Permissions.STAFF)) {
//
//      // Ensure they are allowed in the server
//      if (Workshop.getInstance().getState().getClassroomManager().hasAny()) {
//        if (progressing.isEmpty()) {
//          player.kickPlayer(
//              "There are no classes currently in progress!");
//          return;
//        } else {
//          Optional<Classroom> classroom = progressing.stream()
//              .filter(room ->
//                  room.getParticipants()
//                      .stream()
//                      .anyMatch(part -> part.getPlayerUuid().equals(player.getUniqueId())))
//              .findFirst();
//
//          if (!classroom.isPresent()) {
//            player.kickPlayer("You're not registered for this class: \n"
//                + progressing.get(0).getName() + "\n\n"
//                + "Please contact info@einsteinsworkshop.com if you think this is an error.");
//            Communication.sendStaffMessage(Format.warn("The player "
//                + player.getName()
//                + " just tried to log in but is not registered for a class in session"));
//            return;
//          }
//
//          Classroom.Participant participant = classroom.get().getParticipants()
//              .stream()
//              .filter(part -> part.getPlayerUuid().equals(player.getUniqueId()))
//              .findFirst().get();
//          try {
//            Workshop.getInstance()
//                .getState()
//                .getNicknameManager()
//                .setNickname(participant.getPlayerUuid(),
//                    Format.participantDisplayName(participant, player.getName()));
//          } catch (UnsupportedOperationException e) {
//            Bukkit.getServer().broadcastMessage(Format.info(player.getName() + " is "
//                + ChatColor.GREEN + participant.getFirstName() + " "
//                + participant.getLastName().substring(0, 1) + "."));
//          }
//
//        }
//      }
//
//      if (!progressing.isEmpty()) {
//        player.sendMessage(Format.info("Welcome to "
//            + ChatColor.LIGHT_PURPLE + progressing.get(0).getName()
//            + Format.INFO + "!"));
//      }
//
//
//      // Freeze if necessary
//      if (Workshop.getInstance().getState().getFreezeManager().isAllFrozen()) {
//        Workshop.getInstance().getState().getFreezeManager().freeze(player);
//      }
//
//    }
//  }
//
//}
