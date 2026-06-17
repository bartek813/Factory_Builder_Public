package io.github.bartek813.test_dwa.buildings;

import io.github.bartek813.test_dwa.menus.inventory.Inventory;

public interface InputBuilding extends ConnectableBuilding{
    Inventory getInputInventory();
}
