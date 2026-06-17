package io.github.bartek813.test_dwa.items;

public abstract class Item {
    private final String id;
    private final String displayName;
    private final int maxStackSize;

    public Item(String id, String displayName, int maxStackSize) {
        this.id = id;
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }
}
