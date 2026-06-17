package io.github.bartek813.test_dwa.buildings;

import io.github.bartek813.test_dwa.menus.inventory.Inventory;

public interface OutputBuilding extends ConnectableBuilding {
    Inventory getOutputInventory();
}
