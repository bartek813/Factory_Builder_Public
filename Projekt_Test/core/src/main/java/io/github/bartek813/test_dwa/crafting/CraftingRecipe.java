package io.github.bartek813.test_dwa.crafting;

import io.github.bartek813.test_dwa.items.Item;

public class CraftingRecipe {
    private final String inputItemId;
    private final int inputAmount;

    private final Item outputItem;
    private final int outputAmount;

    private final float craftTime;

    public CraftingRecipe(
        String inputItemId,
        int inputAmount,
        Item outputItem,
        int outputAmount,
        float craftTime
    ) {
        this.inputItemId = inputItemId;
        this.inputAmount = inputAmount;
        this.outputItem = outputItem;
        this.outputAmount = outputAmount;
        this.craftTime = craftTime;
    }

    public String getInputItemId() {
        return inputItemId;
    }

    public int getInputAmount() {
        return inputAmount;
    }

    public Item getOutputItem() {
        return outputItem;
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public float getCraftTime() {
        return craftTime;
    }

}
