package io.github.bartek813.test_dwa.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class Menu {
    private final Stage stage;
    private final Skin skin;

    private final Table mainMenu;
    private final Table savesMenu;

    private boolean open = false;
    private Runnable onResume;
    private Runnable onExit;

    private TextField saveNameField;
    private SelectBox<String> saveSelect;

    private SaveNameAction onSave;
    private SaveNameAction onLoad;

    public Menu() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        mainMenu = new Table();
        mainMenu.setFillParent(true);
        mainMenu.center();

        savesMenu = new Table();
        savesMenu.setFillParent(true);
        savesMenu.center();

        createMainMenu();
        createSavesMenu();

        mainMenu.setVisible(false);
        savesMenu.setVisible(false);

        stage.addActor(mainMenu);
        stage.addActor(savesMenu);
    }

    private void createMainMenu() {
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton savesButton = new TextButton("Save", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        mainMenu.add(resumeButton).width(250).height(60).padTop(10);
        mainMenu.row();

        mainMenu.add(savesButton).width(250).height(60).padTop(10);
        mainMenu.row();

        mainMenu.add(exitButton).width(250).height(60).padTop(10);

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(onResume != null) {
                    onResume.run();
                }
            }
        });

        savesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSavesMenu();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(onExit != null) {
                    onExit.run();
                } else  {
                    Gdx.app.exit();
                }
            }
        });
    }

    private void createSavesMenu(){
        Label titleLabel = new Label("Saves", skin);

        Label saveNameLabel = new Label("Save name:", skin);
        saveNameField = new TextField("", skin);

        TextButton saveButton = new TextButton("Save", skin);

        Label loadLabel = new Label("Load save:", skin);
        saveSelect = new SelectBox<>(skin);

        TextButton loadButton = new TextButton("Load", skin);
        TextButton backButton = new TextButton("Back", skin);

        savesMenu.add(titleLabel).padBottom(20);
        savesMenu.row();

        savesMenu.add(saveNameLabel).padTop(10);
        savesMenu.row();

        savesMenu.add(saveNameField).width(300).height(50).pad(10);
        savesMenu.row();

        savesMenu.add(saveButton).width(250).height(60).pad(10);
        savesMenu.row();

        savesMenu.add(loadLabel).padTop(20);
        savesMenu.row();

        savesMenu.add(saveSelect).width(300).height(50).pad(10);
        savesMenu.row();

        savesMenu.add(loadButton).width(250).height(60).pad(10);
        savesMenu.row();

        savesMenu.add(backButton).width(250).height(60).pad(10);

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(onSave == null) {
                    return;
                }

                String saveName = saveNameField.getText().trim();

                if(saveName.length() == 0) {
                    return;
                }

                onSave.run(saveName);
            }
        });

        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onLoad == null) {
                    return;
                }

                String saveName = saveSelect.getSelected();

                if( saveName == null || saveName.equals("No saves")){
                    return;
                }

                onLoad.run(saveName);
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMainMenu();
            }
        });
    }

    public void open(){
        open = true;
        showMainMenu();
    }

    public void close(){
        open = false;
        mainMenu.setVisible(false);
        savesMenu.setVisible(false);
    }

    public void toggle(){
        if(open){
            close();
        }else{
            open();
        }
    }

    public boolean isOpen(){
        return open;
    }

    public void showMainMenu(){
        mainMenu.setVisible(true);
        savesMenu.setVisible(false);
    }

    public void showSavesMenu(){
        mainMenu.setVisible(false);
        savesMenu.setVisible(true);
    }

    public Stage getStage(){
        return stage;
    }

    public void setOnResume(Runnable onResume){
        this.onResume = onResume;
    }
    public void setOnExit(Runnable onExit){
        this.onExit = onExit;
    }

    public void setOnSave(SaveNameAction onSave){
        this.onSave = onSave;
    }
    public void setOnLoad(SaveNameAction onLoad){
        this.onLoad = onLoad;
    }

    public void update(float deltaTime){
        stage.act(deltaTime);
    }
    public void render(){
        stage.draw();
    }
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public void dispose(){
        stage.dispose();
        skin.dispose();
    }

    public interface SaveNameAction{
        void run(String saveName);
    }

    public void setSaveNames(Array<String> saveNames){
        if( saveNames == null || saveNames.size==0){
            saveSelect.setItems("No saves");
            return;
        }

        saveSelect.setItems(saveNames);
    }
}
