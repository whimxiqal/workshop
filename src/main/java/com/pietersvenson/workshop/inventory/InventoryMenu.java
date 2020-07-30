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

package com.pietersvenson.workshop.inventory;

import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.features.home.HomeCommand;
import com.pietersvenson.workshop.features.spawn.SpawnCommand;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class InventoryMenu {

  public static final InventoryMenu STUDENT_MENU = new InventoryMenu()
      .putItem(new Item(Material.BEACON,
              "Teleport to your home",
              player ->
                  Workshop.getInstance()
                      .getWorkshopCommandTree()
                      .getNode(HomeCommand.class)
                      .ifPresent(command ->
                          player.performCommand(command.getFullCommand()))),
          12)
      .putItem(new Item(Material.BEDROCK,
              "Teleport to spawn",
              player ->
                  Workshop.getInstance()
                      .getWorkshopCommandTree()
                      .getNode(SpawnCommand.class)
                      .ifPresent(command ->
                          player.performCommand(command.getFullCommand()))),
          14);

  private final Item[] items = new Item[27];

  public InventoryMenu() {

  }

  public InventoryMenu putItem(@Nonnull Item item, int slot) throws IndexOutOfBoundsException {
    if (slot < 0 || slot >= 27) {
      throw new IndexOutOfBoundsException("The slot number (" + slot + ") must be between 0 and 26, inclusive");
    }
    items[slot] = item;
    return this;
  }

  public Optional<Item> getItem(int slot) {
    if (slot < 0 || slot >= 27) {
      throw new IndexOutOfBoundsException("The slot number (" + slot + ") must be between 0 and 26, inclusive");
    }
    return Optional.ofNullable(items[slot]);
  }

  public void open(@Nonnull HumanEntity player) {
    Inventory inventory = Bukkit.createInventory(player, InventoryType.CHEST);
    for (int i = 0; i < items.length; i++) {
      if (items[i] != null) {
        ItemStack stack = new ItemStack(items[i].getMaterial());
        ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
        meta.setDisplayName(items[i].getDescription());
        stack.setItemMeta(meta);
        inventory.setItem(i, stack);
      }
    }
    player.openInventory(inventory);
  }

  @Data
  public static class Item implements Consumer<Player> {
    private final Material material;
    private final String description;
    private final Consumer<Player> action;

    @Override
    public void accept(Player player) {
      action.accept(player);
    }
  }

}
