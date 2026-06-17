package io.github.bartek813.test_dwa.menus.inventory;

import io.github.bartek813.test_dwa.items.Item;

public class InventoryTransfer {

    public static void moveOrSwap(InventorySlot from, InventorySlot to) {
        if (from == null || to == null) {
            return;
        }

        if (from == to) {
            return;
        }

        if (from.isEmpty()) {
            return;
        }

        Item fromItem = from.getItem();
        int fromAmount = from.getAmount();

        if (to.isEmpty()) {
            to.set(fromItem, fromAmount);
            from.clear();
            return;
        }

        if (to.getItem().getId().equals(fromItem.getId())) {
            int remaining = to.add(fromItem, fromAmount);
            int moved = fromAmount - remaining;

            if (moved > 0) {
                from.remove(moved);
            }

            return;
        }

        Item toItem = to.getItem();
        int toAmount = to.getAmount();

        to.set(fromItem, fromAmount);
        from.set(toItem, toAmount);
    }
}
