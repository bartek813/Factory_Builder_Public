package io.github.bartek813.test_dwa.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.bartek813.test_dwa.enums.BuildingType;
import io.github.bartek813.test_dwa.technology.TechnologyManager;


public class UnlockMenu {
    private final Stage stage;
    private final Skin skin;
    private final TechnologyManager technologyManager;

    private final Table root;
    private final Table unlockTable;
    private final Label techPointsLabel;

    private boolean open = false;

    public UnlockMenu(TechnologyManager technologyManager) {
        this.technologyManager = technologyManager;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        root = new Table();
        root.setFillParent(true);
        root.center();
        root.pad(20);

        unlockTable = new Table();
        techPointsLabel = new Label("", skin);

        createLayout();

        root.setVisible(false);
        stage.addActor(root);
    }

    private void createLayout() {
        root.clear();

        Label title = new Label("Unlocks", skin);
        TextButton closeButton = new TextButton("Back", skin);

        root.add(title).padBottom(20);
        root.row();

        root.add(techPointsLabel).padBottom(20);
        root.row();

        root.add(unlockTable);
        root.row();

        root.add(closeButton).width(220).height(60).padTop(20);

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });

        refresh();
    }

    private void addUnlockButton(final BuildingType type, String label) {
        boolean unlocked = technologyManager.isUnlocked(type);
        int cost = technologyManager.getUnlockCost(type);

        String text;

        if (unlocked) {
            text = label + " - Unlocked";
        } else {
            text = label + " - " + cost + " tech";
        }

        TextButton button = new TextButton(text, skin);
        button.setDisabled(unlocked);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean unlockedNow = technologyManager.unlock(type);

                if (unlockedNow) {
                    System.out.println("Unlocked: " + type);
                } else {
                    System.out.println("Not enough technology points.");
                }

                refresh();
            }
        });

        unlockTable.add(button).width(340).height(50).pad(5);
        unlockTable.row();
    }

    public void refresh() {
        techPointsLabel.setText("Technology Points: " + technologyManager.getTechnologyPoints());

        unlockTable.clear();

        addUnlockButton(BuildingType.DRILL, "Drill");
        addUnlockButton(BuildingType.EXCAVATOR, "Excavator");
        addUnlockButton(BuildingType.SMELTER, "Smelter");
        addUnlockButton(BuildingType.CARGO_CONTAINER, "Cargo Container");
        addUnlockButton(BuildingType.CONVEYOR, "Conveyor");
        addUnlockButton(BuildingType.CONSTRUCTOR, "Constructor");
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
