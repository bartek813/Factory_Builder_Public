package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.items.Item;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Crafter implements Building{
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final float width;
    private final float height;
    private final float depth;

    private final CraftingRecipe recipe;

    private boolean crafting = false;
    private float craftingProgress = 0f;

    private final Inventory inputInventory = new Inventory(1);
    private final Inventory outputInventory = new Inventory(1);

    private CraftingRecipe selectedRecipe;

    private final Vector3 basePosition = new Vector3();


    public Crafter(Model model, Vector3 position, float width, float height, float depth, CraftingRecipe recipe) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.recipe = recipe;

        modelInstance = new ModelInstance(model);

        modelInstance.transform.setToTranslation(
            position.x,
            position.y + height / 2f,
            position.z
        );

        scene = new Scene(modelInstance);

        basePosition.set(position);

        collisionBounds = new Rectangle(
            position.x - width / 2f,
            position.z - depth / 2f,
            width,
            depth
        );
    }

    public boolean tryStartCrafting(Inventory inventory) {
        if (crafting) {
            System.out.println("Crafting already running");
            return false;
        }

        if(!inventory.hasItem(recipe.getInputItemId(), recipe.getInputAmount())) {
            System.out.println("Not enough resources");
            return false;
        }

        inventory.removeItem(recipe.getInputItemId(), recipe.getInputAmount());

        crafting = true;
        craftingProgress = 0f;

        System.out.println("Crafting started");
        return true;
    }

    public void update(float deltaTime, Inventory inventory) {
        if(!crafting) {
            return;
        }

        craftingProgress += deltaTime;

        if (craftingProgress >= recipe.getCraftTime()) {
            crafting = false;
            craftingProgress = 0f;

            int remaining = inventory.addItem(recipe.getOutputItem(), recipe.getOutputAmount());

            if (remaining > 0) {
                System.out.println("Inventory is full. Could not add all crafted items.");
            } else {
                System.out.println("Crafter finished: " + recipe.getOutputItem().getDisplayName());
            }
        }
    }

    public Inventory getInputInventory() {
        return inputInventory;
    }

    public Inventory getOutputInventory() {
        return outputInventory;
    }

    public void setSelectedRecipe(CraftingRecipe recipe) {
        this.selectedRecipe = recipe;
    }

    public CraftingRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public boolean canCraftSelectedRecipe(){
        if  (selectedRecipe == null) {
            return false;
        }

        if (!inputInventory.hasItem(
            selectedRecipe.getInputItemId(),
            selectedRecipe.getInputAmount()
        )) {
            return false;
        }

        return canAddToOutput(
            selectedRecipe.getOutputItem(),
            selectedRecipe.getOutputAmount()
        );
    }

    public boolean craftSelectedRecipe(){
        if  (!canCraftSelectedRecipe()){
            return false;
        }

        inputInventory.removeItem(
            selectedRecipe.getInputItemId(),
            selectedRecipe.getInputAmount()
        );

        outputInventory.addItem(
            selectedRecipe.getOutputItem(),
            selectedRecipe.getOutputAmount()
        );

        return true;
    }

    boolean canAddToOutput(Item item, int amount) {
        int remaining = amount;

        for (InventorySlot slot : outputInventory.getSlots()) {
            if (slot.isEmpty()){
                return true;
            }

            if (slot.getItem().getId().equals(item.getId())) {
                int freeSpace = slot.getItem().getMaxStackSize() - slot.getAmount();
                remaining -= freeSpace;

                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean collidesWith(Rectangle otherBounds) {
        return collisionBounds.overlaps(otherBounds);
    }

    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    public Scene getScene() {
        return scene;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public boolean isCrafting() {
        return crafting;
    }

    public float getCraftingProgress() {
        return craftingProgress;
    }

    public float getCraftTime() {
        return recipe.getCraftTime();
    }

    public Vector3 getPosition(Vector3 out) {
        modelInstance.transform.getTranslation(out);
        return out;
    }

    public Vector3 getBasePosition(Vector3 out) {
        return out.set(basePosition);
    }
}
