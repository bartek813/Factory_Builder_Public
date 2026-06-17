package io.github.bartek813.test_dwa.menus.inventory;

import io.github.bartek813.test_dwa.items.Item;

public class InventorySlot {
    private Item item;
    private int amount;

    public InventorySlot() {
        this.item = null;
        this.amount = 0;
    }

    public boolean isEmpty() {
        return item == null || amount <= 0;
    }

    public boolean canStack(Item otherItem) {
        if (isEmpty()) {
            return false;
        }

        return item.getId().equals(otherItem.getId()) && amount < item.getMaxStackSize();
    }

    public int add(Item itemToAdd, int amountToAdd) {
        if (amountToAdd <= 0) {
            return 0;
        }

        if (isEmpty()) {
            item = itemToAdd;

            int added = Math.min(amountToAdd, item.getMaxStackSize());
            amount = added;

            return amountToAdd - added;
        }

        if (!canStack(itemToAdd)) {
            return amountToAdd;
        }

        int freeSpace = item.getMaxStackSize() - amount;
        int added = Math.min(amountToAdd, freeSpace);

        amount += added;

        return amountToAdd - added;
    }

    public int remove(int amountToRemove) {
        if (isEmpty() || amountToRemove <= 0) {
            return 0;
        }

        int removed = Math.min(amountToRemove, amount);
        amount -= removed;

        if (amount <= 0) {
            clear();
        }

        return removed;
    }

    public void clear() {
        item = null;
        amount = 0;
    }

    public void set(Item item, int amount) {
        this.item = item;
        this.amount = amount;

        if (this.amount <= 0) {
            clear();
        }
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }
}
