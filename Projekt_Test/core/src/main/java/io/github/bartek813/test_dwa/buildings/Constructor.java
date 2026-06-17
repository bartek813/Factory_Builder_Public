package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.enums.PortType;
import io.github.bartek813.test_dwa.items.IronPlate;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Constructor implements InputBuilding, OutputBuilding{
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final Inventory inputInventory = new Inventory(1);
    private final Inventory outputInventory = new Inventory(1);

    private final Array<BuildingPort> inputPorts = new Array<>();
    private final Array<BuildingPort> outputPorts = new Array<>();

    private final Vector3 basePosition = new Vector3();

    private final float width;
    private final float height;
    private final float depth;

    private final Array<CraftingRecipe> recipes = new Array<>();
    private CraftingRecipe selectedRecipe;
    private float craftTimer = 0f;

    public Constructor(Model model, Vector3 position, float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        basePosition.set(position);

        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(
            position.x,
            position.y + height / 2f,
            position.z
        );

        scene = new Scene(modelInstance);

        collisionBounds = new Rectangle(
            position.x - width / 2f,
            position.z - depth / 2f,
            width,
            depth
        );

        inputPorts.add(new BuildingPort(
            PortType.INPUT,
            new Vector3(-width / 2f - 1f, 0f, 0f)
        ));

        outputPorts.add(new BuildingPort(
            PortType.OUTPUT,
            new Vector3(width / 2f + 1f, 0f, 0f)
        ));

        CraftingRecipe ironPlateRecipe = new CraftingRecipe(
            "iron_ingot",
            2,
            new IronPlate(),
            2,
            2f
        );

        recipes.add(ironPlateRecipe);
        selectedRecipe = ironPlateRecipe;

    }

    public void update(float deltaTime) {
        if (selectedRecipe == null) {
            craftTimer = 0f;
            return;
        }

        if (!canCraftSelectedRecipe()) {
            craftTimer = 0f;
            return;
        }

        craftTimer += deltaTime;

        if (craftTimer >= selectedRecipe.getCraftTime()) {
            craftTimer = 0f;
            craftSelectedRecipe();
        }
    }

    public boolean canCraftSelectedRecipe() {
        if (!hasInputItems()) {
            return false;
        }

        return canAddToOutput(
            selectedRecipe.getOutputItem(),
            selectedRecipe.getOutputAmount()
        );
    }

    private boolean hasInputItems() {
        int total = 0;

        for (InventorySlot slot : inputInventory.getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            if (slot.getItem().getId().equals(selectedRecipe.getInputItemId())) {
                total += slot.getAmount();
            }
        }

        return total >= selectedRecipe.getInputAmount();
    }

    private boolean canAddToOutput(io.github.bartek813.test_dwa.items.Item item, int amount) {
        int remaining = amount;

        for (InventorySlot slot : outputInventory.getSlots()) {
            if (slot.isEmpty()) {
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

    private void craftSelectedRecipe() {
        removeInputItems(
            selectedRecipe.getInputItemId(),
            selectedRecipe.getInputAmount()
        );

        outputInventory.addItem(
            selectedRecipe.getOutputItem(),
            selectedRecipe.getOutputAmount()
        );
    }

    private void removeInputItems(String itemId, int amount) {
        int remaining = amount;

        for (InventorySlot slot : inputInventory.getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            if (!slot.getItem().getId().equals(itemId)) {
                continue;
            }

            int removed = slot.remove(remaining);
            remaining -= removed;

            if (remaining <= 0) {
                return;
            }
        }
    }

    public float getCraftProgress() {
        if (selectedRecipe == null || selectedRecipe.getCraftTime() <= 0f) {
            return 0f;
        }

        return craftTimer / selectedRecipe.getCraftTime();
    }

    public CraftingRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public Array<CraftingRecipe> getRecipes() {
        return recipes;
    }

    public void setSelectedRecipe(CraftingRecipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
        this.craftTimer = 0f;
    }



    @Override
    public Inventory getInputInventory() {
        return inputInventory;
    }

    @Override
    public Inventory getOutputInventory() {
        return outputInventory;
    }

    @Override
    public Array<BuildingPort> getInputPorts() {
        return inputPorts;
    }

    @Override
    public Array<BuildingPort> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    @Override
    public Vector3 getPosition(Vector3 out) {
        modelInstance.transform.getTranslation(out);
        return out;
    }

    @Override
    public Vector3 getBasePosition(Vector3 out) {
        return out.set(basePosition);
    }

    @Override
    public boolean collidesWith(Rectangle otherBounds) {
        return collisionBounds.overlaps(otherBounds);
    }
}
