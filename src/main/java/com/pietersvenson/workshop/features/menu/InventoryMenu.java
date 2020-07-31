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

package com.pietersvenson.workshop.features.menu;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.features.home.HomeCommand;
import com.pietersvenson.workshop.features.spawn.SpawnCommand;
import com.pietersvenson.workshop.features.teleport.TeleportAcceptCommand;
import com.pietersvenson.workshop.features.teleport.TeleportRequestCommand;
import com.pietersvenson.workshop.util.Glowing;
import com.pietersvenson.workshop.util.Randomer;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InventoryMenu {

  private static final Item PLACEHOLDER = new Item(
      Material.GRAY_STAINED_GLASS_PANE,
      " ",
      () -> {
      },
      () -> true,
      false);

  private static final Item EMPTY = new Item(
      Material.WHITE_STAINED_GLASS_PANE,
      " ",
      () -> {
      },
      () -> true,
      false);

  public static final InventoryMenu easyMenu(Player player) {
    InventoryMenu out = new InventoryMenu("Workshop Menu", 6);
    out.putItem(new Item(Material.BEACON,
            "Teleport Home",
            () -> Workshop.getInstance()
                .getWorkshopCommandTree()
                .getNode(HomeCommand.class)
                .ifPresent(command ->
                    player.performCommand(command.getFullCommand())),
            Settings.ENABLE_HOMES::getValue,
            true),
        11);

    out.putItem(new Item(Material.BEDROCK,
            "Teleport to Spawn",
            () -> Workshop.getInstance()
                .getWorkshopCommandTree()
                .getNode(SpawnCommand.class)
                .ifPresent(command ->
                    player.performCommand(command.getFullCommand())),
            Settings.ENABLE_SPAWN::getValue,
            true),
        13);

    Optional<UUID> latestRequester = Workshop.getInstance().getState()
        .getTeleportManager()
        .incoming(player.getUniqueId())
        .entrySet()
        .stream()
        .filter(entry -> !Workshop.getInstance().getState().getTeleportManager().expired(entry.getKey(), player.getUniqueId()))
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey);

    String otherUsername = latestRequester.map(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName()).orElse("unknown");
    out.putItem(new Item(Material.GLOWSTONE,
            "Accept TP Request from " + otherUsername,
            () -> Workshop.getInstance()
                .getWorkshopCommandTree()
                .getNode(TeleportAcceptCommand.class)
                .ifPresent(command ->
                    player.performCommand(command.getFullCommand() + " " + otherUsername)),
            () -> Settings.ENABLE_TELEPORTING.getValue() && latestRequester.isPresent(),
            true),
        15);

    List<Item> teleportItems = Bukkit.getOnlinePlayers().stream()
        .filter(other -> !other.getUniqueId().equals(player.getUniqueId()))
        .limit(27)
        .map(other ->
            new Item(Randomer.randomConcrete(),
                "Request TP to " + other.getName(),
                () -> Workshop.getInstance()
                    .getWorkshopCommandTree()
                    .getNode(TeleportRequestCommand.class)
                    .ifPresent(command ->
                        player.performCommand(command.getFullCommand() + " " + other.getName())),
                Settings.ENABLE_TELEPORTING::getValue,
                true))
        .collect(Collectors.toList());
    for (int i = 0; i < teleportItems.size(); i++) {
      out.putItem(teleportItems.get(i), 27 + i);
    }
    for (int i = teleportItems.size(); i < 27; i++) {
      out.putItem(PLACEHOLDER, 27 + i);
    }
    return out;
  }

  private final Item[] items;
  private final String title;

  public InventoryMenu(String title, int rows) {
    if (rows < 1 || rows > 6) {
      throw new IndexOutOfBoundsException("An inventory can only have between 0 and 6 rows");
    }
    this.title = title;
    this.items = new Item[rows * 9];
    Arrays.fill(items, EMPTY);
  }

  public InventoryMenu putItem(@Nonnull Item item, int slot) throws IndexOutOfBoundsException {
    if (slot < 0 || slot >= items.length) {
      throw new IndexOutOfBoundsException("The slot number (" + slot + ") must be between 0 and " + (items.length - 1) + ", inclusive");
    }
    items[slot] = item;
    return this;
  }

  public Optional<Item> getItem(int slot) {
    if (slot == -999) {
      return Optional.empty();
    }
    if (slot < 0 || slot >= items.length) {
      throw new IndexOutOfBoundsException("The slot number (" + slot + ") must be between 0 and " + (items.length - 1) + ", inclusive");
    }
    return Optional.ofNullable(items[slot]);
  }

  public void open(@Nonnull HumanEntity player) {
    Inventory inventory = Bukkit.createInventory(player, items.length, title);
    for (int i = 0; i < items.length; i++) {
      if (items[i] != null) {
        inventory.setItem(i, (items[i].isActive() ? items[i] : PLACEHOLDER).getStack());
      }
    }
    player.openInventory(inventory);
  }

  @Data
  public static class Item implements Runnable {
    private final Material material;
    private final String description;
    private final Runnable action;
    private final Supplier<Boolean> enabler;
    private final boolean glowing;

    @Override
    public void run() {
      if (isActive()) {
        action.run();
      } else {
        PLACEHOLDER.run();
      }
    }

    public boolean isActive() {
      return enabler.get();
    }

    public ItemStack getStack() {
      ItemStack stack = new ItemStack(getMaterial());
      ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
      meta.setDisplayName(getDescription());
      if (glowing) {
        NamespacedKey key = new NamespacedKey(Workshop.getInstance(), "glow");
        Glowing.Glow glow = new Glowing.Glow(key);
        meta.addEnchant(glow, 1, true);
      }
      stack.setItemMeta(meta);
      return stack;
    }
  }

}
