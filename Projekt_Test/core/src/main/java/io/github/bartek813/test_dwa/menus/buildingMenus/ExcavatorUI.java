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
import io.github.bartek813.test_dwa.buildings.Excavator;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.menus.inventory.InventoryTransfer;

public class ExcavatorUI {

    private final Stage stage;
    private final Skin skin;

    private final Inventory playerInventory;

    private final Table root;
    private final Table playerSlotsTable;
    private final Table outputSlotsTable;

    private Excavator currentExcavator;
    private InventorySlot selectedSlot = null;

    private boolean open = false;

    private Runnable onTakeAll;

    public ExcavatorUI(Inventory playerInventory){
        this.playerInventory = playerInventory;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        root = new Table();
        root.setFillParent(true);
        root.center();
        root.pad(20);

        playerSlotsTable = new Table();
        outputSlotsTable = new Table();

        createLayout();

        root.setVisible(false);
        stage.addActor(root);
    }

    private void createLayout() {
        Table playerPanel = new Table(skin);
        Table excavatorPanel = new Table(skin);

        Label playerTitle = new Label("Player Inventory", skin);
        Label excavatorTitle = new Label("Excavator Output", skin);
        Label infoLabel = new Label("Inputs: 0   Outputs: 1", skin);

        TextButton takeAllButton = new TextButton("Take All", skin);

        playerPanel.add(playerTitle).padBottom(10);
        playerPanel.row();
        playerPanel.add(playerSlotsTable);

        excavatorPanel.add(excavatorTitle).padBottom(10);
        excavatorPanel.row();
        excavatorPanel.add(infoLabel).padBottom(10);
        excavatorPanel.row();
        excavatorPanel.add(outputSlotsTable);
        excavatorPanel.row();
        excavatorPanel.add(takeAllButton).width(200).height(50).padTop(15);

        root.add(playerPanel).padRight(60);
        root.add(excavatorPanel);

        takeAllButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onTakeAll != null) {
                    onTakeAll.run();
                }
            }
        });
    }

    public void open(Excavator excavator){
        currentExcavator = excavator;
        selectedSlot = null;
        open = true;
        root.setVisible(true);
        refresh();
    }

    public void close(){
        currentExcavator = null;
        selectedSlot = null;
        open = false;
        root.setVisible(false);
    }

    public boolean isOpen(){
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

    private void refreshOutputInventory() {
        outputSlotsTable.clear();

        if (currentExcavator == null) {
            return;
        }

        int columns = 3;

        for (int i = 0; i < currentExcavator.getOutputInventory().getSlots().size; i++) {
            InventorySlot slot = currentExcavator.getOutputInventory().getSlots().get(i);

            Table slotTable = createSlotTable(slot);

            outputSlotsTable.add(slotTable).width(100).height(100).pad(5);

            if ((i + 1) % columns == 0) {
                outputSlotsTable.row();
            }
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

    public void refresh() {
        refreshPlayerInventory();
        refreshOutputInventory();
    }

    public void update(float deltaTime) {
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

    public Excavator getCurrentExcavator() {
        return currentExcavator;
    }

    public void setOnTakeAll(Runnable onTakeAll) {
        this.onTakeAll = onTakeAll;
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

}
