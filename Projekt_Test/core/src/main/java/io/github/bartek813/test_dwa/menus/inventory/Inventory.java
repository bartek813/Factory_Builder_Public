package io.github.bartek813.test_dwa.menus.inventory;

import com.badlogic.gdx.utils.Array;
import io.github.bartek813.test_dwa.items.Item;


public class Inventory {
    private final Array<InventorySlot> slots;

    public Inventory(int size) {
        slots = new Array<>();

        for (int i = 0; i < size; i++) {
            slots.add(new InventorySlot());
        }
    }

    public int addItem(Item item, int amount) {
        if (item == null || amount <= 0) {
            return amount;
        }

        int remaining = amount;

        for (InventorySlot slot : slots) {
            if (slot.canStack(item)) {
                remaining = slot.add(item, remaining);

                if (remaining <= 0) {
                    return 0;
                }
            }
        }

        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                remaining = slot.add(item, remaining);

                if (remaining <= 0) {
                    return 0;
                }
            }
        }

        return remaining;
    }

    public boolean hasItem(String itemId, int amount) {
        return getItemCount(itemId) >= amount;
    }

    public int getItemCount(String itemId) {
        int count = 0;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().getId().equals(itemId)) {
                count += slot.getAmount();
            }
        }

        return count;
    }

    public boolean removeItem(String itemId, int amount) {
        if (!hasItem(itemId, amount)) {
            return false;
        }

        int remaining = amount;

        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem().getId().equals(itemId)) {
                int removed = slot.remove(remaining);
                remaining -= removed;

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return true;
    }

    public Array<InventorySlot> getSlots() {
        return slots;
    }

    public void printInventory() {
        System.out.println("Inventory:");

        for (int i = 0; i < slots.size; i++) {
            InventorySlot slot = slots.get(i);

            if (slot.isEmpty()) {
                System.out.println(i + ": empty");
            } else {
                System.out.println(
                    i + ": " +
                        slot.getItem().getDisplayName() +
                        " x" +
                        slot.getAmount()
                );
            }
        }
    }

    public void clear(){
        for (InventorySlot slot : slots) {
            slot.clear();
        }
    }
}
