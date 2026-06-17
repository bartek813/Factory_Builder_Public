package io.github.bartek813.test_dwa.menus.buildingMenus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.bartek813.test_dwa.buildings.Crafter;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.items.IronIngot;
import io.github.bartek813.test_dwa.items.IronPlate;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.menus.inventory.InventoryTransfer;
import jdk.internal.misc.PreviewFeatures;

public class CrafterUI {
    private final Stage stage;
    private final Skin skin;

    private final Inventory playerInventory;

    private final Table root;
    private final Table playerSlotsTable;
    private final Table inputSlotTable;
    private final Table outputSlotTable;
    private final Table recipesTable;

    private final Label selectedRecipeLabel;
    private final Label craftProgressLabel;
    private final TextButton craftButton;

    private final Array<CraftingRecipe> recipes = new Array<>();

    private Crafter currentCrafter;
    private InventorySlot selectedSlot = null;

    private boolean open = false;
    private boolean craftButtonHeld = false;
    private float craftHoldTime = 0f;

    public CrafterUI(Inventory playerInventory){
        this.playerInventory = playerInventory;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        root = new Table();
        root.setFillParent(true);
        root.center();
        root.pad(20);

        playerSlotsTable = new Table();
        inputSlotTable = new Table();
        outputSlotTable = new Table();
        recipesTable = new Table();

        selectedRecipeLabel = new Label("No recipe selected", skin);
        craftProgressLabel = new Label("Hold Craft", skin);
        craftButton = new TextButton("Craft", skin);

        createRecipes();
        createLayout();

        root.setVisible(false);
        stage.addActor(root);
    }

    private void createRecipes(){
        recipes.add(new CraftingRecipe(
            "iron_ore",
            2,
            new IronIngot(),
            1,
            3f
        ));

        recipes.add(new CraftingRecipe(
            "iron_ingot",
            2,
            new IronPlate(),
            2,
            2f
        ));
    }

    private void createLayout() {
        Table playerPanel = new Table(skin);
        Table crafterPanel = new Table(skin);
        Table recipePanel = new Table(skin);

        Label playerTitle = new Label("Player Inventory", skin);
        Label crafterTitle = new Label("Crafter", skin);
        Label recipesTitle = new Label("Recipes", skin);

        playerPanel.add(playerTitle).padBottom(10);
        playerPanel.row();
        playerPanel.add(playerSlotsTable);

        crafterPanel.add(crafterTitle).padBottom(10).colspan(2);
        crafterPanel.row();

        crafterPanel.add(new Label("Input", skin)).padBottom(5);
        crafterPanel.add(new Label("Output", skin)).padBottom(5);
        crafterPanel.row();

        crafterPanel.add(inputSlotTable).width(110).height(110).pad(10);
        crafterPanel.add(outputSlotTable).width(110).height(110).pad(10);
        crafterPanel.row();

        crafterPanel.add(selectedRecipeLabel).colspan(2).padTop(10);
        crafterPanel.row();

        crafterPanel.add(craftProgressLabel).colspan(2).padTop(5);
        crafterPanel.row();

        crafterPanel.add(craftButton).width(220).height(60).colspan(2).padTop(10);

        recipePanel.add(recipesTitle).padBottom(10);
        recipePanel.row();
        recipePanel.add(recipesTable);

        root.add(playerPanel).padRight(60);
        root.add(crafterPanel).padRight(60);
        root.add(recipePanel);

        craftButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                craftButtonHeld = true;
                craftHoldTime = 0f;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                craftButtonHeld = false;
                craftHoldTime = 0f;
                updateCraftProgressLabel();
            }
        });
    }

    public void open(Crafter crafter){
        currentCrafter = crafter;
        open = true;
        craftButtonHeld = false;
        craftHoldTime = 0f;

        root.setVisible(true);

        if (currentCrafter.getSelectedRecipe() == null && recipes.size > 0) {
            currentCrafter.setSelectedRecipe(recipes.first());
        }

        refresh();
    }

    public void close(){
        currentCrafter = null;
        selectedSlot = null;
        open = false;
        craftButtonHeld = false;
        craftHoldTime = 0f;
        root.setVisible(false);
    }

    public boolean isOpen(){
        return open;
    }

    private void refreshPlayerInventory(){
        playerSlotsTable.clear();

        int columns = 4;

        for (int i = 0; i < playerInventory.getSlots().size; i++) {
            InventorySlot slot = playerInventory.getSlots().get(i);

            Table slotTable = createSlotTable(slot);
            playerSlotsTable.add(slotTable).width(100).height(100).pad(5);

            if ((i + 1) % columns == 0) {
                playerSlotsTable.row();
            }
        }
    }

    private void refreshCrafterSlots() {
        inputSlotTable.clear();
        outputSlotTable.clear();

        if (currentCrafter == null) {
            return;
        }

        InventorySlot inputSlot = currentCrafter.getInputInventory().getSlots().get(0);
        InventorySlot outputSlot = currentCrafter.getOutputInventory().getSlots().get(0);

        inputSlotTable.add(createSlotTable(inputSlot)).width(100).height(100);
        outputSlotTable.add(createSlotTable(outputSlot)).width(100).height(100);
    }

    private void refreshRecipes() {
        recipesTable.clear();

        for (final CraftingRecipe recipe : recipes) {
            TextButton recipeButton = new TextButton(
                recipe.getOutputItem().getDisplayName(),
                skin
            );

            recipeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentCrafter != null) {
                        currentCrafter.setSelectedRecipe(recipe);
                        craftButtonHeld = false;
                        craftHoldTime = 0f;
                        refresh();
                    }
                }
            });

            recipesTable.add(recipeButton).width(220).height(50).pad(5);
            recipesTable.row();
        }
    }

    private Table createSlotTable(final InventorySlot slot) {
        Table slotTable = new Table(skin);
        slotTable.defaults().pad(2);
        slotTable.background("default-round");

        if (slot.isEmpty()) {
            slotTable.add(new Label("Empty", skin)).center();
        } else {
            slotTable.add(new Label(slot.getItem().getDisplayName(), skin)).center();
            slotTable.row();
            slotTable.add(new Label("x" + slot.getAmount(), skin)).center();
        }

        slotTable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleSlotClick(slot);
            }
        });

        return slotTable;
    }

    private void handleSlotClick(InventorySlot clickedSlot) {
        if (clickedSlot == null) {
            return;
        }

        if (selectedSlot == null) {
            if (!clickedSlot.isEmpty()) {
                selectedSlot = clickedSlot;
                System.out.println("Selected slot: " + clickedSlot.getItem().getDisplayName());
            }

            return;
        }

        InventoryTransfer.moveOrSwap(selectedSlot, clickedSlot);

        selectedSlot = null;
        refresh();
    }

    private void updateSelectedRecipeLabel() {
        if (currentCrafter == null || currentCrafter.getSelectedRecipe() == null) {
            selectedRecipeLabel.setText("No recipe selected");
            return;
        }

        CraftingRecipe recipe = currentCrafter.getSelectedRecipe();

        selectedRecipeLabel.setText(
            recipe.getInputAmount() +
                "x " +
                recipe.getInputItemId() +
                " -> " +
                recipe.getOutputAmount() +
                "x " +
                recipe.getOutputItem().getDisplayName()
        );
    }

    private void updateCraftProgressLabel() {
        if (currentCrafter == null || currentCrafter.getSelectedRecipe() == null) {
            craftProgressLabel.setText("Hold Craft");
            return;
        }

        CraftingRecipe recipe = currentCrafter.getSelectedRecipe();

        if (!craftButtonHeld) {
            craftProgressLabel.setText("Hold Craft");
            return;
        }

        craftProgressLabel.setText(
            String.format("%.1f / %.1f s", craftHoldTime, recipe.getCraftTime())
        );
    }

    private void updateCrafting(float deltaTime) {
        if (!open || currentCrafter == null || !craftButtonHeld) {
            return;
        }

        CraftingRecipe recipe = currentCrafter.getSelectedRecipe();

        if (recipe == null) {
            craftButtonHeld = false;
            craftHoldTime = 0f;
            updateCraftProgressLabel();
            return;
        }

        if (!currentCrafter.canCraftSelectedRecipe()) {
            craftButtonHeld = false;
            craftHoldTime = 0f;
            craftProgressLabel.setText("Missing resources or output full");
            return;
        }

        craftHoldTime += deltaTime;
        updateCraftProgressLabel();

        if (craftHoldTime >= recipe.getCraftTime()) {
            boolean crafted = currentCrafter.craftSelectedRecipe();

            craftButtonHeld = false;
            craftHoldTime = 0f;

            if (crafted) {
                craftProgressLabel.setText("Crafted");
            } else {
                craftProgressLabel.setText("Cannot craft");
            }

            refresh();
        }
    }

    public void refresh() {
        refreshPlayerInventory();
        refreshCrafterSlots();
        refreshRecipes();
        updateSelectedRecipeLabel();
        updateCraftProgressLabel();
    }

    public void update(float deltaTime) {
        updateCrafting(deltaTime);
        stage.act(deltaTime);
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
