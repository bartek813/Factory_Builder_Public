package io.github.bartek813.test_dwa.items;

public class ItemFactory {
    public static Item createItem(String itemId) {
        switch (itemId) {
            case "iron_ore":
                return new IronOre();

            case "iron_ingot":
                return new IronIngot();

            case "iron_plate":
                return new IronPlate();

            default:
                throw new IllegalArgumentException("Unknown item id: " + itemId);
        }
    }
}
