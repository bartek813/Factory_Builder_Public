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
import io.github.bartek813.test_dwa.buildings.Vendor;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.technology.TechnologyManager;
import io.github.bartek813.test_dwa.technology.VendorPrices;

public class VendorUI {
    private final Stage stage;
    private final Skin skin;

    private final Inventory playerInventory;
    private final TechnologyManager technologyManager;
    private final VendorPrices vendorPrices = new VendorPrices();

    private final Table root;
    private final Table playerSlotsTable;
    private final Label techPointsLabel;

    private Vendor currentVendor;
    private boolean open = false;

    public VendorUI(Inventory playerInventory, TechnologyManager technologyManager) {
        this.playerInventory = playerInventory;
        this.technologyManager = technologyManager;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        root = new Table();
        root.setFillParent(true);
        root.center();
        root.pad(20);

        playerSlotsTable = new Table();
        techPointsLabel = new Label("", skin);

        createLayout();

        root.setVisible(false);
        stage.addActor(root);
    }

    private void createLayout() {
        Table playerPanel = new Table(skin);
        Table vendorPanel = new Table(skin);

        Label playerTitle = new Label("Player Inventory", skin);
        Label vendorTitle = new Label("Vendor", skin);

        TextButton sellAllButton = new TextButton("Sell All", skin);

        playerPanel.add(playerTitle).padBottom(10);
        playerPanel.row();
        playerPanel.add(playerSlotsTable);

        vendorPanel.add(vendorTitle).padBottom(10);
        vendorPanel.row();
        vendorPanel.add(techPointsLabel).padBottom(10);
        vendorPanel.row();
        vendorPanel.add(new Label("Prices:", skin)).padBottom(5);
        vendorPanel.row();
        vendorPanel.add(new Label("Iron Ore = 1 tech", skin));
        vendorPanel.row();
        vendorPanel.add(new Label("Iron Ingot = 3 tech", skin));
        vendorPanel.row();
        vendorPanel.add(new Label("Iron Plate = 5 tech", skin));
        vendorPanel.row();
        vendorPanel.add(sellAllButton).width(220).height(60).padTop(15);

        root.add(playerPanel).padRight(60);
        root.add(vendorPanel);

        sellAllButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sellAllItems();
            }
        });
    }

    private void sellAllItems() {
        int gainedTech = 0;

        for (InventorySlot slot : playerInventory.getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            int price = vendorPrices.getPrice(slot.getItem().getId());

            if (price <= 0) {
                continue;
            }

            gainedTech += price * slot.getAmount();
            slot.clear();
        }

        technologyManager.addTechnologyPoints(gainedTech);

        System.out.println("Sold items for " + gainedTech + " technology points.");

        refresh();
    }

    public void open(Vendor vendor) {
        currentVendor = vendor;
        open = true;
        root.setVisible(true);
        refresh();
    }

    public void close() {
        currentVendor = null;
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

            playerSlotsTable.add(slotTable).width(100).height(100).pad(5);

            if ((i + 1) % columns == 0) {
                playerSlotsTable.row();
            }
        }
    }

    public void refresh() {
        techPointsLabel.setText("Technology Points: " + technologyManager.getTechnologyPoints());
        refreshPlayerInventory();
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

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
