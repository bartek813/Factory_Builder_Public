package io.github.bartek813.test_dwa.menus.buildingMenus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.bartek813.test_dwa.buildings.Smelter;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.menus.inventory.InventoryTransfer;


public class SmelterUI {
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

    private Smelter currentSmelter;
    private InventorySlot selectedSlot = null;

    private boolean open = false;
    private float refreshTimer = 0f;

    public SmelterUI(Inventory playerInventory) {
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
        craftButton = new TextButton("Crafting", skin);
        craftButton.setDisabled(true);

        createLayout();

        root.setVisible(false);
        stage.addActor(root);
    }

    private void createLayout() {
        Table playerPanel = new Table(skin);
        Table smelterPanel = new Table(skin);
        Table recipePanel = new Table(skin);

        Label playerTitle = new Label("Player Inventory", skin);
        Label smelterTitle = new Label("Smelter", skin);
        Label recipesTitle = new Label("Recipes", skin);

        playerPanel.add(playerTitle).padBottom(10);
        playerPanel.row();
        playerPanel.add(playerSlotsTable);

        smelterPanel.add(smelterTitle).padBottom(10).colspan(2);
        smelterPanel.row();

        smelterPanel.add(new Label("Input", skin)).padBottom(5);
        smelterPanel.add(new Label("Output", skin)).padBottom(5);
        smelterPanel.row();

        smelterPanel.add(inputSlotTable).width(110).height(110).pad(10);
        smelterPanel.add(outputSlotTable).width(110).height(110).pad(10);
        smelterPanel.row();

        smelterPanel.add(selectedRecipeLabel).colspan(2).padTop(10);
        smelterPanel.row();

        smelterPanel.add(craftProgressLabel).colspan(2).padTop(5);
        smelterPanel.row();

        smelterPanel.add(craftButton).width(220).height(60).colspan(2).padTop(10);

        recipePanel.add(recipesTitle).padBottom(10);
        recipePanel.row();
        recipePanel.add(recipesTable);

        root.add(playerPanel).padRight(60);
        root.add(smelterPanel).padRight(60);
        root.add(recipePanel);

        craftButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                updateCraftProgressLabel();
            }
        });
    }

    public void open(Smelter smelter) {
        currentSmelter = smelter;
        selectedSlot = null;
        refreshTimer = 0f;


        open = true;
        root.setVisible(true);

        refresh();
    }

    public void close() {
        currentSmelter = null;
        selectedSlot = null;
        refreshTimer = 0f;


        open = false;
        root.setVisible(false);
    }

    public boolean isOpen() {
        return open;
    }

    private void refreshPlayerInventory() {
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

    private void refreshSmelterSlots() {
        inputSlotTable.clear();
        outputSlotTable.clear();

        if (currentSmelter == null) {
            return;
        }

        InventorySlot inputSlot = currentSmelter.getInputInventory().getSlots().get(0);
        InventorySlot outputSlot = currentSmelter.getOutputInventory().getSlots().get(0);

        inputSlotTable.add(createSlotTable(inputSlot)).width(100).height(100);
        outputSlotTable.add(createSlotTable(outputSlot)).width(100).height(100);
    }

    private void refreshRecipes() {
        recipesTable.clear();

        if (currentSmelter == null) {
            return;
        }

        for (final CraftingRecipe recipe : currentSmelter.getRecipes()) {
            TextButton recipeButton = new TextButton(
                recipe.getOutputItem().getDisplayName(),
                skin
            );

            recipeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    currentSmelter.setSelectedRecipe(recipe);

                    refresh();
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
        if (currentSmelter == null || currentSmelter.getSelectedRecipe() == null) {
            selectedRecipeLabel.setText("No recipe selected");
            return;
        }

        CraftingRecipe recipe = currentSmelter.getSelectedRecipe();

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
        if (currentSmelter == null || currentSmelter.getSelectedRecipe() == null) {
            craftProgressLabel.setText("No recipe selected");
            return;
        }

        if (currentSmelter.canCraftSelectedRecipe()) {
            craftProgressLabel.setText("Working automatically");
        } else {
            craftProgressLabel.setText("Waiting for input or output space");
        }
    }

    public void refresh() {
        refreshPlayerInventory();
        refreshSmelterSlots();
        refreshRecipes();
        updateSelectedRecipeLabel();
        updateCraftProgressLabel();
    }

    public void update(float deltaTime) {
        if (open && selectedSlot == null) {
            refreshTimer += deltaTime;

            if (refreshTimer >= 0.25f) {
                refreshTimer = 0f;
                refresh();
            }
        }

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

    public Smelter getCurrentSmelter() {
        return currentSmelter;
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
