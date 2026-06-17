package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.enums.PortType;
import io.github.bartek813.test_dwa.items.IronIngot;
import io.github.bartek813.test_dwa.items.Item;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Smelter implements InputBuilding, OutputBuilding {

    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final Inventory inputInventory;
    private final Inventory outputInventory;

    private final Array<BuildingPort> inputPorts = new Array<>();
    private final Array<BuildingPort> outputPorts = new Array<>();

    private final Array<CraftingRecipe> recipes = new Array<>();
    private CraftingRecipe selectedRecipe;

    private float craftTimer = 0f;

    private final Vector3 basePosition = new Vector3();

    private final float width;
    private final float height;
    private final float depth;

    public Smelter(Model model, Vector3 position, float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        inputInventory = new Inventory(1);
        outputInventory = new Inventory(1);

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
            new Vector3(0f, 0f, depth / 2f - 1f)
        ));

        outputPorts.add(new BuildingPort(
            PortType.OUTPUT,
            new Vector3(0f, 0f, depth / 2f + 1f)
        ));

        createRecipes();

        if (recipes.size > 0) {
            selectedRecipe = recipes.first();
        }
    }

    private void createRecipes() {
        recipes.add(new CraftingRecipe(
            "iron_ore",
            1,
            new IronIngot(),
            1,
            3f
        ));
    }

    public boolean canCraftSelectedRecipe() {
        if (selectedRecipe == null) {
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

    public boolean craftSelectedRecipe() {
        if (!canCraftSelectedRecipe()) {
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

    private boolean canAddToOutput(Item item, int amount) {
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

    public void update(float deltaTime) {
        if (selectedRecipe == null){
            craftTimer = 0f;
            return;
        }

        if (!canCraftSelectedRecipe()) {
            craftTimer = 0f;
            return;
        }

        craftTimer += deltaTime;

        if(craftTimer >= selectedRecipe.getCraftTime()) {
            craftTimer = 0f;
            craftSelectedRecipe();
        }
    }

    public Inventory getInputInventory() {
        return inputInventory;
    }

    public Inventory getOutputInventory() {
        return outputInventory;
    }

    public Array<CraftingRecipe> getRecipes() {
        return recipes;
    }

    public CraftingRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(CraftingRecipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
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
    public boolean collidesWith(Rectangle otherBounds) {
        return collisionBounds.overlaps(otherBounds);
    }

    @Override
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    @Override
    public Scene getScene() {
        return scene;
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
}
