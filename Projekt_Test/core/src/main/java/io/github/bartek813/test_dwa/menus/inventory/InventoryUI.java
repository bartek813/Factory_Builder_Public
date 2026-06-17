package io.github.bartek813.test_dwa.menus.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class InventoryUI {
    private final Stage stage;
    private final Skin skin;
    private final Inventory inventory;

    private final Table root;
    private final Table slotsTable;

    private InventorySlot selectedSlot = null;

    private boolean open = false;

    public InventoryUI(Inventory inventory){
        this.inventory = inventory;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(20);

        Label titleLabel = new Label("Inventory", skin);
        root.add(titleLabel).left().padBottom(10);
        root.row();

        slotsTable = new Table();
        root.add(slotsTable).left();

        root.setVisible(false);
        stage.addActor(root);

        refresh();

    }

    private Table createSlotTable(final InventorySlot slot){
        Table slotTable = new Table(skin);
        slotTable.defaults().pad(2);
        slotTable.background("default-round");

        if(slot.isEmpty()){
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

    private void handleSlotClick(InventorySlot clickedSlot){
        if (clickedSlot == null){
            return;
        }

        if (selectedSlot == null){
            if(!clickedSlot.isEmpty()){
                selectedSlot = clickedSlot;
                System.out.println("Selected slot: " + clickedSlot.getItem().getDisplayName());
            }

            return;
        }

        InventoryTransfer.moveOrSwap(selectedSlot, clickedSlot);

        selectedSlot = null;

        refresh();
    }

    public void toggle() {
        if(open){
            close();
        } else {
            open();
        }
    }

    public void open() {
        open = true;
        root.setVisible(true);
        refresh();
    }

    public void close() {
        open = false;
        root.setVisible(false);
    }

    public boolean isOpen() {
        return open;
    }

    public void refresh(){
        slotsTable.clear();

        int columns = 4;

        for (int i = 0; i < inventory.getSlots().size; i++) {
            InventorySlot slot = inventory.getSlots().get(i);

            Table slotTable = createSlotTable(slot);

            slotsTable.add(slotTable).width(100).height(100).pad(5);

            if ((i + 1) % columns == 0) {
                slotsTable.row();
            }
        }
    }

    public Stage getStage() {
        return stage;
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

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

}
