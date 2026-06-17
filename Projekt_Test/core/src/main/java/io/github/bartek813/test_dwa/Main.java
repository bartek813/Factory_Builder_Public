package io.github.bartek813.test_dwa;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.bartek813.test_dwa.buildings.*;
import io.github.bartek813.test_dwa.crafting.CraftingRecipe;
import io.github.bartek813.test_dwa.enums.BuildingType;
import io.github.bartek813.test_dwa.enums.CameraMode;
import io.github.bartek813.test_dwa.items.IronIngot;
import io.github.bartek813.test_dwa.items.IronOre;
import io.github.bartek813.test_dwa.items.ItemFactory;
import io.github.bartek813.test_dwa.menus.UnlockMenu;
import io.github.bartek813.test_dwa.menus.buildingMenus.*;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.menus.inventory.InventoryUI;
import io.github.bartek813.test_dwa.menus.Menu;
import io.github.bartek813.test_dwa.resourceNodes.IronNode;
import io.github.bartek813.test_dwa.resourceNodes.ResourceNode;
import io.github.bartek813.test_dwa.saves.BuildingSaveData;
import io.github.bartek813.test_dwa.saves.ConveyorSaveData;
import io.github.bartek813.test_dwa.saves.SaveData;
import io.github.bartek813.test_dwa.saves.SaveManager;
import io.github.bartek813.test_dwa.shaders.CustomShaderProvider;
import io.github.bartek813.test_dwa.technology.TechnologyManager;
import io.github.bartek813.test_dwa.terrains.HeightMapTerrain;
import io.github.bartek813.test_dwa.terrains.Terrain;
import io.github.bartek813.test_dwa.terrains.TerrainFloatAttribute;
import io.github.bartek813.test_dwa.terrains.TerrainMaterial;
import io.github.bartek813.test_dwa.terrains.attributes.TerrainMaterialAttribute;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


public class Main extends ApplicationAdapter implements AnimationController.AnimationListener, InputProcessor
{
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene playerScene;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private float time;
    private SceneSkybox skybox;
    private DirectionalLightEx light;
    private FirstPersonCameraController cameraController;


    // saves
    private SaveManager saveManager;

    // load
    private boolean loadingGame = false;

    // menu
    private Menu menu;

    // inventory
    private Inventory inventory;
    private InventoryUI inventoryUI;

    // building
    private int selectedBuildingType = 0;
    private Vector3 tempBuildingPosition = new Vector3();

    // mining
    private float manualMineCooldown = 0f;
    private static final float MANUAL_MINE_TIME = 0.25f;

    // crafter
    private CrafterUI crafterUI;

    // resource nodes
    private Model ironNodeModel;
    private final Array<ResourceNode> resourceNodes = new Array<>();

    // drill
    private Model drillModel;
    private final Array<Drill> drills = new Array<>();
    private DrillUI drillUI;

    // excavator
    private Model excavatorModel;
    private final Array<Excavator> excavators = new Array<>();
    private ExcavatorUI excavatorUI;
    private final Vector3 tempExcavatorPosition = new Vector3();

    // smelter
    private Model smelterModel;
    private final Array<Smelter> smelters = new Array<>();
    private SmelterUI smelterUI;
    private final Vector3 tempSmelterPosition = new Vector3();

    // cargo container
    private Model cargoContainerModel;
    private final Array<CargoContainer> cargoContainers = new Array<>();
    private CargoContainerUI cargoContainerUI;
    private final Vector3 tempCargoContainerPosition = new Vector3();

    // conveyors
    private Model conveyorModel;
    private final Array<Conveyor> conveyors = new Array<>();

    private boolean conveyorModeActive = false;
    private OutputBuilding selectedConveyorSource = null;

    //Vendor and unlocs
    private TechnologyManager technologyManager;
    private UnlockMenu unlockMenu;

    private Model vendorModel;
    private final Array<Vendor> vendors = new Array<>();
    private VendorUI vendorUI;
    private final Vector3 tempVendorPosition = new Vector3();

    // constructor
    private Model constructorModel;
    private final Array<Constructor> constructors = new Array<>();
    private ConstructorUI constructorUI;
    private final Vector3 tempConstructorPosition = new Vector3();

    // hover building UI
    private Stage hoverStage;
    private Skin hoverSkin;
    private Label hoverBuildingLabel;

    // building positions
    private final Vector3 tempCrafterPosition = new Vector3();
    private final Vector3 tempDrillPosition = new Vector3();

    // remove
    private Model deletePreviewModel;
    private ModelInstance deletePreviewInstance;
    private Scene deletePreviewScene;
    private Smelter deleteTargetSmelter = null;
    private CargoContainer deleteTargetCargoContainer = null;
    private Vendor deleteTargetVendor = null;
    private Constructor deleteTargetConstructor = null;

    private boolean deleteModeActive = false;

    private Crafter deleteTargetCrafter = null;
    private Drill deleteTargetDrill = null;
    private Excavator deleteTargetExcavator = null;

    private final Vector3 tempDeletePosition = new Vector3();

    private static final float DELETE_CLICK_MARGIN = 3f;

    // player movement
    float speed = 5f;
    float rotationSpeed = 80f;
    private final Matrix4 playerTransform = new Matrix4();
    private final Vector3 moveTranslation = new Vector3();
    private final Vector3 currentPosition = new Vector3();

    // player collisions
    private static final float PLAYER_COLLISION_SIZE = 2f;

    private final Rectangle playerCollisionBounds = new Rectangle();
    private final Vector3 proposedPlayerPosition = new Vector3();
    private final Matrix4 proposedPlayerTransform = new Matrix4();

    // jump movement
    float verticalVelocity = 0f;
    float gravity = -25f;
    float jumpPower = 12f;
    boolean isGrounded = true;

    // pick ray
    private final Vector3 pickedPosition = new Vector3();
    private final Vector3 intersection = new Vector3();
    private final Plane groundPlane = new Plane(Vector3.Y, 0f);
    // object placing test
    private Model crafterModel;
    private final Array<Crafter> crafters = new Array<>();


    // pick ray test
    private Model previewBoxModel;
    private ModelInstance previewBoxInstance;
    private Scene previewBoxScene;
    private boolean buildingPreviewActive = false;

    // terrain
    private Terrain terrain;
    private Scene terrainScene;

    // camera
    private CameraMode cameraMode = CameraMode.BEHIND_PLAYER;
    private float camPitch = Settings.CAMERA_START_PITCH;
    private float distanceFromPlayer = 35f;
    private float angleAroundPlayer = 0f;
    private float angleBehindPlayer = 0f;


    @Override
    public void create() {

        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/Alien Slime.gltf"));
        playerScene = new Scene(sceneAsset.scene);
        sceneManager = new SceneManager(new CustomShaderProvider(), PBRShaderProvider.createDefaultDepth(24));
        sceneManager.addScene(playerScene);


        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //float d = .02f;
        camera.near = 1f;
        camera.far = 1000;
        sceneManager.setCamera(camera);
        camera.position.set(0, 0, 4f);

        cameraController = new FirstPersonCameraController(camera);
        cameraController.setVelocity(100f);

        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));

        // setup light
        light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        // animacja
        playerScene.animationController.setAnimation("idle",-1);
        CreateTerrain();

        // Blue box -> pick ray test
        ModelBuilder modelBuilder = new ModelBuilder();

        previewBoxModel = modelBuilder.createBox(
            5f, 5f, 5f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.BLUE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        previewBoxInstance = new ModelInstance(previewBoxModel);
        previewBoxInstance.transform.setToTranslation(0, -1000, 0);

        deletePreviewModel = modelBuilder.createBox(
            1f,1f,1f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        deletePreviewInstance = new ModelInstance(deletePreviewModel);
        deletePreviewInstance.transform.setToTranslation(0, -1000, 0);

        deletePreviewScene = new Scene(deletePreviewInstance);
        sceneManager.addScene(deletePreviewScene);

        // crafter
        crafterModel = modelBuilder.createBox(
            5f,5f,5f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        // Iron Node
        ironNodeModel = modelBuilder.createBox(
            8f,2f,8f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        // drill
        drillModel = modelBuilder.createBox(
            6f,6f,6f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.ORANGE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        // excavator
        excavatorModel = modelBuilder.createBox(
            7f, 6f, 7f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.YELLOW)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        // smelter
        smelterModel = modelBuilder.createBox(
            6f, 5f, 6f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.FIREBRICK)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        // conveyor
        conveyorModel = modelBuilder.createBox(
            1f, 1f, 1f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.BROWN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        cargoContainerModel = modelBuilder.createBox(
            8f, 4f, 5f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.CYAN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        //Vendor
        vendorModel = modelBuilder.createBox(
            5f, 4f, 5f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.PURPLE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        // constructor
        constructorModel = modelBuilder.createBox(
            6f, 4f, 7f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.TEAL)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        createIronNode(525f, 222f);
        createIronNode(625f, 222f);

        previewBoxScene = new Scene(previewBoxInstance);
        sceneManager.addScene(previewBoxScene);

        menu = new Menu();

        menu.setOnSave(new Menu.SaveNameAction(){
            @Override
            public void run(String saveName){
                saveGame(saveName);
                menu.setSaveNames(saveManager.getSaveNames());
            }
        });

        menu.setOnLoad(new Menu.SaveNameAction(){
            @Override
            public void run(String saveName){
                loadGame(saveName);
                closeMenu();
            }
        });

        menu.setOnResume(new Runnable(){
            @Override
            public void run(){
                closeMenu();
            }
        });

        menu.setOnExit(new Runnable(){
            @Override
            public void run(){
                Gdx.app.exit();
            }
        });

        //test inventory
        inventory = new Inventory(12);

        inventoryUI = new InventoryUI(inventory);

        technologyManager = new TechnologyManager();

        vendorUI = new VendorUI(inventory, technologyManager);
        unlockMenu = new UnlockMenu(technologyManager);

        // crafterUI

        crafterUI = new CrafterUI(inventory);

        // drill UI
        drillUI = new DrillUI(inventory);

        // excavatorUI
        excavatorUI = new ExcavatorUI(inventory);

        // smelterUI
        smelterUI = new SmelterUI(inventory);

        // containerUI
        cargoContainerUI = new CargoContainerUI(inventory);

        // constructorUI
        constructorUI = new ConstructorUI(inventory);

        // save
        saveManager = new SaveManager();

        hoverStage = new Stage(new ScreenViewport());
        hoverSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        hoverBuildingLabel = new Label("", hoverSkin);
        hoverBuildingLabel.setVisible(false);
        hoverBuildingLabel.setPosition(20, Gdx.graphics.getHeight() - 40);

        hoverStage.addActor(hoverBuildingLabel);

        menu.setOnResume(new Runnable(){
            @Override
            public void run(){
                closeMenu();
            }
        });

        menu.setOnExit(new Runnable(){
            @Override
            public void run(){
                Gdx.app.exit();
            }
        });

        menu.setOnSave(new Menu.SaveNameAction(){
            @Override
            public void run(String saveName){
                saveGame(saveName);
                menu.setSaveNames(saveManager.getSaveNames());
            }
        });

        menu.setOnLoad(new Menu.SaveNameAction(){
            @Override
            public void run(String saveName){
                loadGame(saveName);
                closeMenu();
            }
        });

        boolean loaded = loadGame(saveManager.getLastSaveName());

        if (!loaded) {
            //inventory.addItem(new IronOre(), 25);
            inventoryUI.refresh();
            System.out.println("Started new game.");
        }

        drillUI.setOnTakeAll(new Runnable(){
            @Override
            public void run(){
                takeAllFromCurrentDrill();
            }
        });

        cargoContainerUI.setOnTakeAll(new Runnable() {
            @Override
            public void run() {
                takeAllFromCurrentCargoContainer();
            }
        });

        inventoryUI.refresh();

        excavatorUI.setOnTakeAll(new Runnable() {
            @Override
            public void run() {
                takeAllFromCurrentExcavator();
            }
        });

    }

    private Rectangle getPlayerBoundAt(Vector3 position){
        playerCollisionBounds.set(
            position.x - PLAYER_COLLISION_SIZE / 2f,
            position.z - PLAYER_COLLISION_SIZE / 2f,
            PLAYER_COLLISION_SIZE,
            PLAYER_COLLISION_SIZE
        );

        return playerCollisionBounds;
    }

    private boolean collidesWithAnyBuilding(Vector3 position){
        Rectangle playerBounds = getPlayerBoundAt(position);

        for (Crafter crafter : crafters){
            if (crafter.getCollisionBounds().overlaps(playerBounds)){
                return true;
            }
        }

        for (Excavator excavator : excavators){
            if (excavator.getCollisionBounds().overlaps(playerBounds)){
                return true;
            }
        }

        for (Smelter smelter : smelters){
            if (smelter.getCollisionBounds().overlaps(playerBounds)){
                return true;
            }
        }

        for (Drill drill : drills){
            if (drill.getCollisionBounds().overlaps(playerBounds)){
                return true;
            }
        }

        for (CargoContainer container : cargoContainers) {
            if (container.getCollisionBounds().overlaps(playerBounds)) {
                return true;
            }
        }

        for (Vendor vendor : vendors) {
            if (vendor.getCollisionBounds().overlaps(playerBounds)) {
                return true;
            }
        }

        for (Constructor constructor : constructors) {
            if (constructor.getCollisionBounds().overlaps(playerBounds)) {
                return true;
            }
        }

        return false;
    }

    private void applyPlayerMovementWithCollision() {
        if (moveTranslation.x == 0f && moveTranslation.z == 0f) {
            return;
        }

        float moveX = moveTranslation.x;
        float moveZ = moveTranslation.z;

        proposedPlayerTransform.set(playerTransform);
        proposedPlayerTransform.translate(moveX, 0f, moveZ);
        proposedPlayerTransform.getTranslation(proposedPlayerPosition);

        if (!collidesWithAnyBuilding(proposedPlayerPosition)) {
            playerTransform.set(proposedPlayerTransform);
            return;
        }

        if (moveX != 0f) {
            proposedPlayerTransform.set(playerTransform);
            proposedPlayerTransform.translate(moveX, 0f, 0f);
            proposedPlayerTransform.getTranslation(proposedPlayerPosition);

            if (!collidesWithAnyBuilding(proposedPlayerPosition)) {
                playerTransform.set(proposedPlayerTransform);
            }
        }

        if (moveZ != 0f) {
            proposedPlayerTransform.set(playerTransform);
            proposedPlayerTransform.translate(0f, 0f, moveZ);
            proposedPlayerTransform.getTranslation(proposedPlayerPosition);

            if (!collidesWithAnyBuilding(proposedPlayerPosition)) {
                playerTransform.set(proposedPlayerTransform);
            }
        }
    }

    // hover on building
    private String findHoveredBuildingName(Vector3 position) {
        for (Crafter crafter : crafters) {
            if (expandedBounds(crafter.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Crafter";
            }
        }

        for (Vendor vendor : vendors) {
            if (expandedBounds(vendor.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Vendor";
            }
        }

        for (Drill drill : drills) {
            if (expandedBounds(drill.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Drill";
            }
        }

        for (Excavator excavator : excavators) {
            if (expandedBounds(excavator.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Excavator";
            }
        }

        for (Smelter smelter : smelters) {
            if (expandedBounds(smelter.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Smelter";
            }
        }

        for (CargoContainer container : cargoContainers) {
            if (expandedBounds(container.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Cargo Container";
            }
        }

        for (Constructor constructor : constructors) {
            if (expandedBounds(constructor.getCollisionBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return "Constructor";
            }
        }

        for (ResourceNode node : resourceNodes) {
            if (expandedBounds(node.getBounds(), DELETE_CLICK_MARGIN).contains(position.x, position.z)) {
                return node.getDisplayName();
            }
        }

        return null;
    }

    private void updateHoverBuildingLabel(float deltaTime) {
        if (hoverBuildingLabel == null) {
            return;
        }

        if (menu.isOpen()
            || inventoryUI.isOpen()
            || drillUI.isOpen()
            || crafterUI.isOpen()
            || excavatorUI.isOpen()
            || smelterUI.isOpen()
            || cargoContainerUI.isOpen()
            || vendorUI.isOpen()
            || unlockMenu.isOpen()) {

            hoverBuildingLabel.setVisible(false);
            hoverStage.act(deltaTime);
            return;
        }

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if (!getMouseWorldPosition(mouseX, mouseY, pickedPosition)) {
            hoverBuildingLabel.setVisible(false);
            hoverStage.act(deltaTime);
            return;
        }

        String buildingName = findHoveredBuildingName(pickedPosition);

        if (buildingName == null) {
            hoverBuildingLabel.setVisible(false);
        } else {
            if (buildingName.equals("Iron Node")) {
                hoverBuildingLabel.setText(buildingName + " - LPM to mine");
            } else if (deleteModeActive) {
                hoverBuildingLabel.setText(buildingName + " - Click to delete");
            } else {
                hoverBuildingLabel.setText(buildingName + " - Press E");
            }

            hoverBuildingLabel.setVisible(true);
        }
        hoverStage.act(deltaTime);
    }

    private void takeAllFromCurrentDrill(){
        if(drillUI == null || drillUI.getCurrentDrill() == null){
            return;
        }

        takeAllFromDrill(drillUI.getCurrentDrill());
    }

    // Menu exec
    private void openMenu(){
        menu.open();

        if (saveManager != null){
            menu.setSaveNames(saveManager.getSaveNames());
        }

        menu.showMainMenu();

        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(menu.getStage());
    }
    private void closeMenu(){
        menu.close();

        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
    }


    private void CreateTerrain() {
        if(terrain != null){
            terrain.dispose();
            sceneManager.removeScene(terrainScene);
        }
        terrain = new HeightMapTerrain(new Pixmap(Gdx.files.internal("textures/heightmap.png")), 60f);
        terrainScene = new Scene(terrain.getModelInstance());
        sceneManager.addScene(terrainScene);
    }
    // create nodes
    private void createIronNode(float x, float z){
        float y = terrain.getHeightAtWorldCoord(x, z);

        Vector3 nodePosition = new Vector3(x,y,z);

        IronNode ironNode = new IronNode(
            ironNodeModel,
            nodePosition
        );

        resourceNodes.add(ironNode);
        sceneManager.addScene(ironNode.getScene());

        System.out.println("Created Iron Node at: " + nodePosition);
    }
    // mining
    private void manuallyMineNode(ResourceNode node) {
        if (manualMineCooldown > 0f) {
            return;
        }

        manualMineCooldown = MANUAL_MINE_TIME;

        if (node == null) {
            return;
        }

        String itemId = node.getResourceItemId();

        if (itemId == null) {
            return;
        }

        int remaining = inventory.addItem(ItemFactory.createItem(itemId), 1);

        if (remaining == 0) {
            inventoryUI.refresh();
            System.out.println("Mined 1x " + itemId + " from " + node.getDisplayName());
        } else {
            System.out.println("Inventory is full.");
        }
    }

    // drill on node
    private Rectangle createDrillBoundsAt(Vector3 position){
        return new Rectangle(
            position.x -6f / 2f,
            position.z - 6f / 2f,
            6f,
            6f
        );
    }
    private ResourceNode findResourceNodeUnderDrill(Vector3 position){
        Rectangle drillBounds = createDrillBoundsAt(position);

        System.out.println("Drill bounds: " + drillBounds);

        for( ResourceNode node : resourceNodes){
            System.out.println("Node bounds: " + node.getBounds());

            if(node. overlaps(drillBounds)){
                System.out.println("Found node under drill: " + node.getDisplayName());
                return node;
            }
        }
        System.out.println("No node under drill.");
        return null;
    }
    // placing drill
    private boolean canPlaceDrill(Vector3 position){
        Rectangle newBounds = createDrillBoundsAt(position);

        for(Crafter crafter : crafters){
            if(crafter.collidesWith(newBounds)){
                return false;
            }
        }
        for (Drill drill : drills){
            if(drill.collidesWith(newBounds)){
                return false;
            }
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) {
                return false;
            }
        }

        for (CargoContainer cargoContainer : cargoContainers) {
            if (cargoContainer.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) {
                return false;
            }
        }
        ResourceNode node = findResourceNodeUnderDrill(position);

        return node != null;
    }
    private void placeDrill(Vector3 position){

        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.DRILL)) {
            System.out.println("Drill is locked.");
            return;
        }

        ResourceNode node = findResourceNodeUnderDrill(position);

        if(node == null){
            System.out.println("Cannot place drill here. No resource node under drill.");
            return;
        }

        if(!canPlaceDrill(position)){
            System.out.println("Cannot place drill here. Place is occupied.");
            return;
        }

        if(!loadingGame && !payBuildingCost("iron_ingot", 2)){
            return;
        }

        Drill drill = new Drill(
            drillModel,
            position,
            6f,
            6f,
            6f,
            node
        );

        drills.add(drill);
        sceneManager.addScene(drill.getScene());

        System.out.println("Drill placed on: " + node.getDisplayName());
    }
    //excavator
    private Rectangle createExcavatorBoundsAt(Vector3 position) {
        return new Rectangle(
            position.x - 7f / 2f,
            position.z - 7f / 2f,
            7f,
            7f
        );
    }

    private ResourceNode findResourceNodeUnderExcavator(Vector3 position) {
        Rectangle excavatorBounds = createExcavatorBoundsAt(position);

        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(excavatorBounds)) {
                return node;
            }
        }

        return null;
    }

    private ResourceNode findResourceNodeAt(Vector3 position) {
        for (ResourceNode node : resourceNodes) {
            Rectangle bounds = expandedBounds(node.getBounds(), 2f);

            if (bounds.contains(position.x, position.z)) {
                return node;
            }
        }

        return null;
    }

    private boolean canPlaceExcavator(Vector3 position) {
        Rectangle newBounds = createExcavatorBoundsAt(position);

        for (Crafter crafter : crafters) {
            if (crafter.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Drill drill : drills) {
            if (drill.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) {
                return false;
            }
        }

        return findResourceNodeUnderExcavator(position) != null;
    }

    private void placeExcavator(Vector3 position) {
        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.EXCAVATOR)) {
            System.out.println("Smelter is locked.");
            return;
        }

        ResourceNode node = findResourceNodeUnderExcavator(position);

        if (node == null) {
            System.out.println("Cannot place excavator here. No resource node under excavator.");
            return;
        }

        if (!canPlaceExcavator(position)) {
            System.out.println("Cannot place excavator here. Place is occupied.");
            return;
        }

        if(!loadingGame && !payBuildingCost("iron_plate", 2)){
            return;
        }

        Excavator excavator = new Excavator(
            excavatorModel,
            position,
            7f,
            6f,
            7f,
            node
        );

        excavators.add(excavator);
        sceneManager.addScene(excavator.getScene());

        System.out.println("Excavator placed on: " + node.getDisplayName());
    }

    private void updateExcavators(float deltaTime) {
        for (Excavator excavator : excavators) {
            excavator.update(deltaTime);
        }
    }
    private void updateSmelters(float deltaTime) {
        for (Smelter smelter : smelters) {
            smelter.update(deltaTime);
        }
    }

    private Excavator findNearestExcavator(float maxDistance){
        Excavator nearestExcavator = null;
        float nearestDistance = maxDistance;

        for (Excavator excavator : excavators) {
            excavator.getPosition(tempExcavatorPosition);

            float distance = currentPosition.dst(tempExcavatorPosition);

            if(distance <= nearestDistance){
                nearestDistance = distance;
                nearestExcavator = excavator;
            }
        }

        return nearestExcavator;
    }

    private void takeAllFromCurrentExcavator() {
        if (excavatorUI == null || excavatorUI.getCurrentExcavator() == null) {
            return;
        }

        takeAllFromExcavator(excavatorUI.getCurrentExcavator());
    }

    private void takeAllFromExcavator(Excavator excavator) {
        if (excavator == null) {
            return;
        }

        for (InventorySlot slot : excavator.getOutputInventory().getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            int amountBefore = slot.getAmount();

            int remaining = inventory.addItem(slot.getItem(), amountBefore);
            int moved = amountBefore - remaining;

            if (moved > 0) {
                slot.remove(moved);
            }

            if (remaining > 0) {
                System.out.println("Player inventory is full. Could not move all items.");
                break;
            }
        }

        inventoryUI.refresh();
        excavatorUI.refresh();
    }

    // conveyor utility
    private OutputBuilding findOutputBuildingAt(Vector3 position) {
        OutputBuilding nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Excavator excavator : excavators) {
            Rectangle bounds = expandedBounds(excavator.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                excavator.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = excavator;
                }
            }
        }

        for (Smelter smelter : smelters) {
            Rectangle bounds = expandedBounds(smelter.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                smelter.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = smelter;
                }
            }
        }

        for (CargoContainer container : cargoContainers) {
            Rectangle bounds = expandedBounds(container.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                container.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = container;
                }
            }
        }

        for (Constructor constructor : constructors) {
            Rectangle bounds = expandedBounds(constructor.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                constructor.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = constructor;
                }
            }
        }

        return nearest;
    }

    private InputBuilding findInputBuildingAt(Vector3 position) {
        InputBuilding nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Smelter smelter : smelters) {
            Rectangle bounds = expandedBounds(smelter.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                smelter.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if(distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = smelter;
                }
            }
        }

        for (CargoContainer container : cargoContainers) {
            Rectangle bounds = expandedBounds(container.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                container.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = container;
                }
            }
        }

        for (Constructor constructor : constructors) {
            Rectangle bounds = expandedBounds(constructor.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                constructor.getPosition(tempBuildingPosition);
                float distance = position.dst(tempBuildingPosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = constructor;
                }
            }
        }

        return nearest;
    }
    // conveyor
    private void placeConveyor(OutputBuilding source, InputBuilding target) {
        if (source == null || target == null) {
            return;
        }

        if (source == target) {
            System.out.println("Cannot connect building to itself.");
            return;
        }

        Conveyor conveyor = new Conveyor(
            conveyorModel,
            source,
            target
        );

        conveyors.add(conveyor);
        sceneManager.addScene(conveyor.getScene());

        System.out.println("Conveyor connected.");
    }
    private void updateConveyors(float deltaTime) {
        for (Conveyor conveyor : conveyors) {
            conveyor.update(deltaTime);
        }

    }

    // hiding BuildBox
    private void hideBuildingPreview(){
        buildingPreviewActive = false;

        previewBoxInstance.transform.setToTranslation(0, -1000, 0);

    }

    private void updateBuildingPreview(){
        if(!buildingPreviewActive){
            return;
        }
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if(getMouseWorldPosition(mouseX, mouseY, pickedPosition)){
            previewBoxInstance.transform.setToTranslation(pickedPosition.x, pickedPosition.y+2.5f, pickedPosition.z);
        }
    }

    // hiding RemoveBox
    private void hideDeletePreview(){
        deletePreviewInstance.transform.idt();
        deletePreviewInstance.transform.setToTranslation(0, -1000, 0);

        deleteTargetCrafter = null;
        deleteTargetDrill = null;
        deleteTargetExcavator = null;
        deleteTargetSmelter = null;
        deleteTargetCargoContainer = null;
        deleteTargetVendor = null;
        deleteTargetConstructor = null;
    }

    private void closeDeleteMode(){
        deleteModeActive = false;
        hideDeletePreview();
    }

    private Rectangle expandedBounds(Rectangle bounds, float margin){
        return new Rectangle(
            bounds.x - margin,
            bounds.y - margin,
            bounds.width + margin * 2f,
            bounds.height + margin * 2f
        );
    }

    private boolean findDeleteTargetAt(Vector3 position){
        deleteTargetCrafter = null;
        deleteTargetDrill = null;
        deleteTargetExcavator = null;
        deleteTargetSmelter = null;
        deleteTargetCargoContainer = null;
        deleteTargetVendor = null;
        deleteTargetConstructor = null;

        float nearestDistance = Float.MAX_VALUE;

        for(Crafter crafter : crafters){
            Rectangle bounds = expandedBounds(crafter.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if(bounds.contains(position.x, position.z)){
                crafter.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if(distance < nearestDistance){
                    nearestDistance = distance;
                    deleteTargetCrafter = crafter;
                    deleteTargetDrill = null;
                }
            }
        }

        for (Drill drill : drills) {
            Rectangle bounds = expandedBounds(drill.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                drill.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    deleteTargetCrafter = null;
                    deleteTargetDrill = drill;
                }
            }
        }

        for (Excavator excavator : excavators) {
            Rectangle bounds = expandedBounds(excavator.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                excavator.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    deleteTargetCrafter = null;
                    deleteTargetDrill = null;
                    deleteTargetExcavator = excavator;
                }
            }
        }

        for (Smelter smelter : smelters) {
            Rectangle bounds = expandedBounds(smelter.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                smelter.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    deleteTargetCrafter = null;
                    deleteTargetDrill = null;
                    deleteTargetExcavator = null;
                    deleteTargetSmelter = smelter;
                }
            }
        }

        for (CargoContainer container : cargoContainers) {
            Rectangle bounds = expandedBounds(container.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                container.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    deleteTargetCrafter = null;
                    deleteTargetDrill = null;
                    deleteTargetExcavator = null;
                    deleteTargetSmelter = null;
                    deleteTargetCargoContainer = container;
                }
            }
        }

        for (Vendor vendor : vendors) {
            Rectangle bounds = expandedBounds(vendor.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                vendor.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    deleteTargetCrafter = null;
                    deleteTargetDrill = null;
                    deleteTargetExcavator = null;
                    deleteTargetSmelter = null;
                    deleteTargetCargoContainer = null;
                    deleteTargetVendor = vendor;
                }
            }
        }

        for (Constructor constructor : constructors) {
            Rectangle bounds = expandedBounds(constructor.getCollisionBounds(), DELETE_CLICK_MARGIN);

            if (bounds.contains(position.x, position.z)) {
                constructor.getPosition(tempDeletePosition);

                float distance = position.dst(tempDeletePosition);

                if (distance < nearestDistance) {
                    nearestDistance = distance;

                    deleteTargetCrafter = null;
                    deleteTargetDrill = null;
                    deleteTargetExcavator = null;
                    deleteTargetSmelter = null;
                    deleteTargetConstructor = constructor;
                    deleteTargetCargoContainer = null;
                    deleteTargetVendor = null;
                }
            }
        }

        return deleteTargetCrafter != null
            || deleteTargetDrill != null
            || deleteTargetExcavator != null
            || deleteTargetSmelter != null
            || deleteTargetCargoContainer != null
            || deleteTargetVendor != null
            || deleteTargetConstructor != null;
    }

    // smelter
    private Rectangle createSmelterBoundsAt(Vector3 position) {
        return new Rectangle(
            position.x - 6f / 2f,
            position.z - 6f / 2f,
            6f,
            6f
        );
    }

    private boolean canPlaceSmelter(Vector3 position) {
        Rectangle newBounds = createSmelterBoundsAt(position);

        for(Crafter crafter : crafters){
            if(crafter.collidesWith(newBounds)){
                return false;
            }
        }
        for (Drill drill : drills){
            if(drill.collidesWith(newBounds)){
                return false;
            }
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) {
                return false;
            }
        }

        for (CargoContainer cargoContainer : cargoContainers) {
            if (cargoContainer.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) {
                return false;
            }
        }
        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(newBounds)) {
                return false;
            }
        }

        return true;
    }

    private void placeSmelter(Vector3 position) {
        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.SMELTER)) {
            System.out.println("Smelter is locked.");
            return;
        }


        if (!canPlaceSmelter(position)) {
            System.out.println("Cannot place smelter here. Place is occupied.");
            return;
        }

        if (!loadingGame && !payBuildingCost("iron_plate", 4)){
            return;
        }

        Smelter smelter = new Smelter(
            smelterModel,
            position,
            6f,
            5f,
            6f
        );

        smelters.add(smelter);
        sceneManager.addScene(smelter.getScene());

        System.out.println("Smelter placed at: " + position);
    }
    private Smelter findNearestSmelter(float maxDistance) {
        Smelter nearestSmelter = null;
        float nearestDistance = maxDistance;

        for (Smelter smelter : smelters) {
            smelter.getPosition(tempSmelterPosition);

            float distance = currentPosition.dst(tempSmelterPosition);

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestSmelter = smelter;
            }
        }

        return nearestSmelter;
    }

    private void updateDeletePreview(){
        if(!deleteModeActive){
            return;
        }

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if (!getMouseWorldPosition(mouseX, mouseY, pickedPosition)){
            hideDeletePreview();
            return;
        }

        if(!findDeleteTargetAt(pickedPosition)){
            hideDeletePreview();
            return;
        }

        if(deleteTargetCrafter != null){
            deleteTargetCrafter.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(5f,5f,5f);
            return;
        }

        if(deleteTargetDrill != null){
            deleteTargetDrill.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(6f,6f,6f);
            return;
        }

        if (deleteTargetExcavator != null) {
            deleteTargetExcavator.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(7f, 6f, 7f);
            return;
        }
        if (deleteTargetSmelter != null) {
            deleteTargetSmelter.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(6f, 5f, 6f);
            return;
        }

        if (deleteTargetCargoContainer != null) {
            deleteTargetCargoContainer.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(8f, 4f, 5f);
            return;
        }

        if (deleteTargetVendor != null) {
            deleteTargetVendor.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(5f, 4f, 5f);
            return;
        }

        if (deleteTargetConstructor != null) {
            deleteTargetConstructor.getPosition(tempDeletePosition);

            deletePreviewInstance.transform.idt();
            deletePreviewInstance.transform.setToTranslation(
                tempDeletePosition.x,
                tempDeletePosition.y,
                tempDeletePosition.z
            );
            deletePreviewInstance.transform.scale(6f, 4f, 7f);
            return;
        }

    }

    private void placeCrafter(Vector3 position) {
        if (!canPlaceCrafter(position)) {
            System.out.println("Cannot place crafter here. Place is occupied.");
            return;
        }
        if (!loadingGame && !payBuildingCost("iron_ore", 2)) {
            return;
        }

        CraftingRecipe recipe = new CraftingRecipe(
            "iron_ore",
            2,
            new IronIngot(),
            1,
            3f
        );
        Crafter crafter = new Crafter(
            crafterModel,
            position,
            5f,
            5f,
            5f,
            recipe
        );

        sceneManager.addScene(crafter.getScene());
        crafters.add(crafter);

        System.out.println("Crafter placed at: " + position);
    }
    private void updateCrafter(float deltaTime){
        for(Crafter crafter : crafters){
            crafter.update(deltaTime, inventory);
        }

        if (inventoryUI != null & inventoryUI.isOpen()) {
            inventoryUI.refresh();
        }
    }
    private void updateDrills(float deltaTime){
        for (Drill drill : drills){
            drill.update(deltaTime);
        }
    }

    private Drill findNearestDrill(float maxDistance){
        Drill nearestDrill = null;
        float nearestDistance = maxDistance;

        for(Drill drill : drills){
            drill.getPosition(tempDrillPosition);

            float distance = currentPosition.dst(tempDrillPosition);

            if(distance <= nearestDistance){
                nearestDistance = distance;
                nearestDrill = drill;
            }
        }

        return nearestDrill;
    }
    // take from drill
    private void takeAllFromDrill(Drill drill){
        if(drill == null){
            return;
        }

        for (InventorySlot slot: drill.getInventory().getSlots()){
            if(slot.isEmpty()){
                continue;
            }

            int amountBefore = slot.getAmount();

            int remaining = inventory.addItem(slot.getItem(), amountBefore);
            int moved = amountBefore - remaining;

            if (moved > 0 ){
                slot.remove(moved);
            }

            if(remaining > 0){
                System.out.println("Player inventory is full. Could not move all items.");
                break;
            }
        }

        inventoryUI.refresh();
        drillUI.refresh();
    }

    private Rectangle createBoundsAt(Vector3 position, float width, float depth) {
        return new Rectangle(
            position.x - width / 2f,
            position.z - depth / 2f,
            width,
            depth
        );
    }
    private boolean canPlaceCrafter(Vector3 position) {
        Rectangle newBounds = createBoundsAt(position, 5f, 5f);

        for(Crafter crafter : crafters){
            if(crafter.collidesWith(newBounds)){
                return false;
            }
        }
        for (Drill drill : drills){
            if(drill.collidesWith(newBounds)){
                return false;
            }
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) {
                return false;
            }
        }

        for (CargoContainer cargoContainer : cargoContainers) {
            if (cargoContainer.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) {
                return false;
            }
        }
        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(newBounds)) {
                return false;
            }
        }


        return true;
    }
    private Crafter findNearestCrafter(float maxDistance){
        Crafter nearestCrafter = null;
        float nearestDistance = maxDistance;

        for (Crafter crafter : crafters) {
            crafter.getPosition(tempCrafterPosition);

            float distance = currentPosition.dst(tempCrafterPosition);

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestCrafter = crafter;
            }
        }

        return nearestCrafter;
    }

    private Rectangle createCargoContainerBoundsAt(Vector3 position) {
        return new Rectangle(
            position.x - 8f / 2f,
            position.z - 5f / 2f,
            8f,
            5f
        );
    }

    private boolean canPlaceCargoContainer(Vector3 position) {
        Rectangle newBounds = createCargoContainerBoundsAt(position);

        for(Crafter crafter : crafters){
            if(crafter.collidesWith(newBounds)){
                return false;
            }
        }
        for (Drill drill : drills){
            if(drill.collidesWith(newBounds)){
                return false;
            }
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) {
                return false;
            }
        }

        for (CargoContainer cargoContainer : cargoContainers) {
            if (cargoContainer.collidesWith(newBounds)) {
                return false;
            }
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) {
                return false;
            }
        }
        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(newBounds)) {
                return false;
            }
        }

        return true;
    }

    private void placeCargoContainer(Vector3 position) {
        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.CARGO_CONTAINER)) {
            System.out.println("Smelter is locked.");
            return;
        }

        if (!canPlaceCargoContainer(position)) {
            System.out.println("Cannot place cargo container here. Place is occupied.");
            return;
        }

        if (!loadingGame && !payBuildingCost("iron_plate",8)){
            return;
        }

        CargoContainer container = new CargoContainer(
            cargoContainerModel,
            position,
            8f,
            4f,
            5f
        );

        cargoContainers.add(container);
        sceneManager.addScene(container.getScene());

        System.out.println("Cargo Container placed at: " + position);
    }
    private CargoContainer findNearestCargoContainer(float maxDistance) {
        CargoContainer nearestContainer = null;
        float nearestDistance = maxDistance;

        for (CargoContainer container : cargoContainers) {
            container.getPosition(tempCargoContainerPosition);

            float distance = currentPosition.dst(tempCargoContainerPosition);

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestContainer = container;
            }
        }

        return nearestContainer;
    }

    private Rectangle createConstructorBoundsAt(Vector3 position) {
        return new Rectangle(
            position.x - 6f / 2f,
            position.z - 7f / 2f,
            6f,
            7f
        );
    }

    private boolean canPlaceConstructor(Vector3 position) {
        Rectangle newBounds = createConstructorBoundsAt(position);

        for (Crafter crafter : crafters) {
            if (crafter.collidesWith(newBounds)) return false;
        }

        for (Drill drill : drills) {
            if (drill.collidesWith(newBounds)) return false;
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) return false;
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) return false;
        }

        for (Constructor constructor : constructors) {
            if (constructor.collidesWith(newBounds)) return false;
        }

        for (CargoContainer container : cargoContainers) {
            if (container.collidesWith(newBounds)) return false;
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) return false;
        }

        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(newBounds)) return false;
        }

        return true;
    }

    private void placeConstructor(Vector3 position) {
        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.CONSTRUCTOR)) {
            System.out.println("Constructor is locked.");
            return;
        }

        if (!canPlaceConstructor(position)) {
            System.out.println("Cannot place constructor here. Place is occupied.");
            return;
        }

        if (!loadingGame && !payBuildingCost("iron_plate", 6)) {
            return;
        }

        Constructor constructor = new Constructor(
            constructorModel,
            position,
            6f,
            4f,
            7f
        );

        constructors.add(constructor);
        sceneManager.addScene(constructor.getScene());

        System.out.println("Constructor placed at: " + position);
    }
    private void updateConstructors(float deltaTime) {
        for (Constructor constructor : constructors) {
            constructor.update(deltaTime);
        }
    }

    private Constructor findNearestConstructor(float maxDistance) {
        Constructor nearestConstructor = null;
        float nearestDistance = maxDistance;

        for (Constructor constructor : constructors) {
            constructor.getPosition(tempConstructorPosition);

            float distance = currentPosition.dst(tempConstructorPosition);

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestConstructor = constructor;
            }
        }

        return nearestConstructor;
    }


    private OutputBuilding findSavedOutputBuilding(String type, int index) {
        if (type == null || index < 0) {
            return null;
        }

        if (type.equals("excavator") && index < excavators.size) {
            return excavators.get(index);
        }

        if (type.equals("smelter") && index < smelters.size) {
            return smelters.get(index);
        }

        if (type.equals("cargo_container") && index < cargoContainers.size) {
            return cargoContainers.get(index);
        }

        if (type.equals("constructor") && index < constructors.size) {
            return constructors.get(index);
        }

        return null;
    }

    private InputBuilding findSavedInputBuilding(String type, int index) {
        if (type == null || index < 0) {
            return null;
        }

        if (type.equals("smelter") && index < smelters.size) {
            return smelters.get(index);
        }

        if (type.equals("cargo_container") && index < cargoContainers.size) {
            return cargoContainers.get(index);
        }

        if (type.equals("constructor") && index < constructors.size) {
            return constructors.get(index);
        }

        return null;
    }

    private void removeNearestBuilding(float maxDistance){
        Crafter nearestCrafter = null;
        Drill nearestDrill = null;

        float nearestCrafterDistance = maxDistance;
        float nearestDrillDistance = maxDistance;

        for (Crafter crafter : crafters) {
            crafter.getPosition(tempBuildingPosition);

            float distance = currentPosition.dst(tempBuildingPosition);

            if(distance <= nearestCrafterDistance){
                nearestCrafterDistance = distance;
                nearestCrafter = crafter;
            }
        }

        for(Drill drill : drills){
            drill.getPosition(tempBuildingPosition);

            float distance = currentPosition.dst(tempBuildingPosition);

            if (distance <= nearestDrillDistance) {
                nearestDrillDistance = distance;
                nearestDrill = drill;
            }
        }

        if (nearestCrafter == null && nearestDrill == null) {
            System.out.println("No building nearby");
            return;
        }

        if (nearestCrafter != null && (nearestDrill == null || nearestCrafterDistance <= nearestDrillDistance)) {
            sceneManager.removeScene(nearestCrafter.getScene());
            crafters.removeValue(nearestCrafter, true);

            if (crafterUI != null && crafterUI.isOpen()){
                crafterUI.refresh();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Removed crafter");
            return;
        }

        if (nearestDrill != null) {
            sceneManager.removeScene(nearestDrill.getScene());
            drills.removeValue(nearestDrill, true);

            if (drillUI != null && drillUI.isOpen()) {
                drillUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Drill removed.");
        }

    }

    private void removeDeleteTarget(){
        if (deleteTargetCrafter != null){
            sceneManager.removeScene(deleteTargetCrafter.getScene());
            crafters.removeValue(deleteTargetCrafter, true);

            System.out.println("Delete target crafter.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetDrill != null){
            sceneManager.removeScene(deleteTargetDrill.getScene());
            drills.removeValue(deleteTargetDrill, true);

            System.out.println("Delete target drill.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetExcavator != null) {
            removeConveyorsConnectedTo(deleteTargetExcavator);

            sceneManager.removeScene(deleteTargetExcavator.getScene());
            excavators.removeValue(deleteTargetExcavator, true);

            System.out.println("Delete target excavator.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetSmelter != null) {
            removeConveyorsConnectedTo(deleteTargetSmelter);

            sceneManager.removeScene(deleteTargetSmelter.getScene());
            smelters.removeValue(deleteTargetSmelter, true);

            if (smelterUI != null && smelterUI.isOpen()) {
                smelterUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Delete target smelter.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetCargoContainer != null) {
            removeConveyorsConnectedTo(deleteTargetCargoContainer);

            sceneManager.removeScene(deleteTargetCargoContainer.getScene());
            cargoContainers.removeValue(deleteTargetCargoContainer, true);

            if (cargoContainerUI != null && cargoContainerUI.isOpen()) {
                cargoContainerUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Delete target cargo container.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetVendor != null) {
            sceneManager.removeScene(deleteTargetVendor.getScene());
            vendors.removeValue(deleteTargetVendor, true);

            if (vendorUI != null && vendorUI.isOpen()) {
                vendorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Delete target vendor.");

            hideDeletePreview();
            return;
        }

        if (deleteTargetConstructor != null) {
            removeConveyorsConnectedTo(deleteTargetConstructor);

            sceneManager.removeScene(deleteTargetConstructor.getScene());
            constructors.removeValue(deleteTargetConstructor, true);

            if (constructorUI != null && constructorUI.isOpen()) {
                constructorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }

            System.out.println("Delete target constructor.");

            hideDeletePreview();
            return;
        }
}

    private void removeConveyorsConnectedTo(Object building) {
        if (building == null) {
            return;
        }

        for (int i = conveyors.size - 1; i >= 0; i--) {
            Conveyor conveyor = conveyors.get(i);

            boolean isConnected =
                conveyor.getSource() == building ||
                    conveyor.getTarget() == building;

            if (isConnected) {
                sceneManager.removeScene(conveyor.getScene());
                conveyors.removeIndex(i);

                System.out.println("Removed conveyor connected to deleted building.");
            }
        }
    }

    private void takeAllFromCurrentCargoContainer() {
        if (cargoContainerUI == null || cargoContainerUI.getCurrentContainer() == null) {
            return;
        }

        takeAllFromCargoContainer(cargoContainerUI.getCurrentContainer());
    }

    private void takeAllFromCargoContainer(CargoContainer container) {
        if (container == null) {
            return;
        }

        for (InventorySlot slot : container.getInventory().getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            int amountBefore = slot.getAmount();

            int remaining = inventory.addItem(slot.getItem(), amountBefore);
            int moved = amountBefore - remaining;

            if (moved > 0) {
                slot.remove(moved);
            }

            if (remaining > 0) {
                System.out.println("Player inventory is full. Could not move all items.");
                break;
            }
        }

        inventoryUI.refresh();
        cargoContainerUI.refresh();
    }

    private Rectangle createVendorBoundsAt(Vector3 position) {
        return new Rectangle(
            position.x - 5f / 2f,
            position.z - 5f / 2f,
            5f,
            5f
        );
    }

    private boolean canPlaceVendor(Vector3 position) {
        Rectangle newBounds = createVendorBoundsAt(position);

        for (Crafter crafter : crafters) {
            if (crafter.collidesWith(newBounds)) return false;
        }

        for (Drill drill : drills) {
            if (drill.collidesWith(newBounds)) return false;
        }

        for (Excavator excavator : excavators) {
            if (excavator.collidesWith(newBounds)) return false;
        }

        for (Smelter smelter : smelters) {
            if (smelter.collidesWith(newBounds)) return false;
        }

        for (CargoContainer container : cargoContainers) {
            if (container.collidesWith(newBounds)) return false;
        }

        for (Vendor vendor : vendors) {
            if (vendor.collidesWith(newBounds)) return false;
        }

        for (ResourceNode node : resourceNodes) {
            if (node.overlaps(newBounds)) return false;
        }


        return true;
    }

    private boolean consumeBuildingCost(String itemId, int amount) {
        if (!inventory.hasItem(itemId, amount)) {
            System.out.println("Missing building materials: " + amount + "x " + itemId);
            return false;
        }

        inventory.removeItem(itemId, amount);
        inventoryUI.refresh();

        return true;
    }

    private void placeVendor(Vector3 position) {
        if (!loadingGame && !technologyManager.isUnlocked(BuildingType.VENDOR)) {
            System.out.println("Vendor is locked.");
            return;
        }

        if (!loadingGame && !consumeBuildingCost("iron_ore", 4)) {
            return;
        }

        if (!canPlaceVendor(position)) {
            System.out.println("Cannot place vendor here. Place is occupied.");
            return;
        }

        if (!consumeBuildingCost("iron_ore", 4)) {
            return;
        }

        Vendor vendor = new Vendor(
            vendorModel,
            position,
            5f,
            4f,
            5f
        );

        vendors.add(vendor);
        sceneManager.addScene(vendor.getScene());

        System.out.println("Vendor placed.");
    }
    private Vendor findNearestVendor(float maxDistance) {
        Vendor nearestVendor = null;
        float nearestDistance = maxDistance;

        for (Vendor vendor : vendors) {
            vendor.getPosition(tempVendorPosition);

            float distance = currentPosition.dst(tempVendorPosition);

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestVendor = vendor;
            }
        }

        return nearestVendor;
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);

        if(menu != null){
            menu.resize(width, height);
        }

        if(inventoryUI != null){
            inventoryUI.resize(width, height);
        }

        if(drillUI != null){
            drillUI.resize(width, height);
        }

        if (crafterUI != null) {
            crafterUI.resize(width, height);
        }

        if (excavatorUI != null) {
            excavatorUI.resize(width, height);
        }

        if (smelterUI != null) {
            smelterUI.resize(width, height);
        }

        if (cargoContainerUI != null) {
            cargoContainerUI.resize(width, height);
        }

        if (unlockMenu != null) {
            unlockMenu.resize(width, height);
        }

        if (vendorUI != null) {
            vendorUI.resize(width, height);
        }

        if (constructorUI != null) {
            constructorUI.resize(width, height);
        }

        if (hoverStage != null) {
            hoverStage.getViewport().update(width, height, true);
            hoverBuildingLabel.setPosition(20, height - 40);
        }
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        time += deltaTime;

        processInput(deltaTime);
        updateCamera();
        updateBuildingPreview();

        updateDeletePreview();

        updateHoverBuildingLabel(deltaTime);

        updateCrafter(deltaTime);

        updateDrills(deltaTime);

        updateExcavators(deltaTime);

        updateConveyors(deltaTime);

        updateSmelters(deltaTime);

        updateConstructors(deltaTime);

        if (manualMineCooldown > 0f) {
            manualMineCooldown -= deltaTime;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)){
            CreateTerrain();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            Material mat = terrain.getModelInstance().materials.get(0);
            TerrainMaterial terrainMaterial = ((TerrainMaterialAttribute) mat.get(TerrainMaterialAttribute.TerrainMaterial)).terrainMaterial;
            TerrainFloatAttribute attr = (TerrainFloatAttribute) terrainMaterial.get(TerrainFloatAttribute.MinSlope);
            attr.value += 0.01f;
            attr.value = Math.min(attr.value, 0.9f);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            Material mat = terrain.getModelInstance().materials.get(0);
            TerrainMaterial terrainMaterial = ((TerrainMaterialAttribute) mat.get(TerrainMaterialAttribute.TerrainMaterial)).terrainMaterial;
            TerrainFloatAttribute attr = (TerrainFloatAttribute) terrainMaterial.get(TerrainFloatAttribute.MinSlope);
            attr.value -= 0.01f;
        }

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        menu.update(deltaTime);
        menu.render();

        inventoryUI.update(deltaTime);
        inventoryUI.render();

        drillUI.update(deltaTime);
        drillUI.render();

        crafterUI.update(deltaTime);
        crafterUI.render();

        excavatorUI.update(deltaTime);
        excavatorUI.render();

        smelterUI.update(deltaTime);
        smelterUI.render();

        cargoContainerUI.update(deltaTime);
        cargoContainerUI.render();

        vendorUI.update(deltaTime);
        vendorUI.render();

        constructorUI.update(deltaTime);
        constructorUI.render();

        unlockMenu.update(deltaTime);
        unlockMenu.render();

        hoverStage.draw();

    }
    private void updateCamera(){
        if(cameraMode == CameraMode.FLY_MODE){
            cameraController.update();
            return;
        }
        float horDistance = calculateHorizontalDistance(distanceFromPlayer);
        float verDistance = calculateVerticalDistance(distanceFromPlayer);

        calculatePitch();
        calculateAngleAroundPlayer();
        calculateCameraPosition(currentPosition, horDistance, verDistance);

        float height = terrain.getHeightAtWorldCoord(camera.position.x, camera.position.z);
        if(camera.position.y < height + 10f){
            camera.position.y = height + 10f;
        }

        camera.lookAt(currentPosition);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void calculateCameraPosition(Vector3 currentPosition, float horDistance, float verDistance) {
        float offsetX = (float) (horDistance * Math.sin(Math.toRadians(angleAroundPlayer)));
        float offsetZ = (float) (horDistance * Math.cos(Math.toRadians(angleAroundPlayer)));

        camera.position.x = currentPosition.x - offsetX;
        camera.position.z = currentPosition.z - offsetZ;
        camera.position.y = currentPosition.y + verDistance;
    }

    private void calculateAngleAroundPlayer() {
        if (cameraMode == CameraMode.FREE_LOOK){
            float angleChange = Gdx.input.getDeltaX() * Settings.CAMERA_ANGLE_AROUND_PLAYER_FACTOR;
            angleAroundPlayer -= angleChange;
        }
        else {
            angleAroundPlayer = angleBehindPlayer;
        }
    }

    private void calculatePitch() {
        float pitchChange = -Gdx.input.getDeltaY() * Settings.CAMERA_PITCH_FACTOR;
        camPitch -= pitchChange;

        if (camPitch < Settings.CAMERA_MIN_PITCH) {
            camPitch = Settings.CAMERA_MIN_PITCH;
        }
        else if (camPitch > Settings.CAMERA_MAX_PITCH) {
            camPitch = Settings.CAMERA_MAX_PITCH;
        }
    }

    private float calculateVerticalDistance(float distanceFromPlayer) {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(camPitch)));
    }

    private float calculateHorizontalDistance(float distanceFromPlayer) {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(camPitch)));
    }

    private boolean getMouseWorldPosition(int screenX, int screenY, Vector3 out){
        Ray ray = camera.getPickRay(screenX, screenY);

        if(Intersector.intersectRayPlane(ray, groundPlane, intersection)){
            float terrainHeight = terrain.getHeightAtWorldCoord(intersection.x, intersection.z);

            out.set(
                intersection.x,
                terrainHeight,
                intersection.z
            );

            return true;
        }
        return false;
    }

    private void setPlayerPositionAfterLoad(float x, float y, float z) {
        currentPosition.set(x, y, z);

        verticalVelocity = 0f;
        isGrounded = true;
        moveTranslation.set(0, 0, 0);


        playerScene.modelInstance.transform.idt();
        playerScene.modelInstance.transform.setTranslation(currentPosition);

        playerTransform.set(playerScene.modelInstance.transform);


        cameraMode = CameraMode.BEHIND_PLAYER;
        angleBehindPlayer = 0f;
        angleAroundPlayer = angleBehindPlayer;

        updateCamera();
    }

    private boolean loadGame(String saveName) {
        loadingGame = true;

        SaveData saveData = saveManager.loadGame(saveName);

        if (saveData == null) {
            loadingGame = false;
            return false;
        }

        saveManager.loadInventory(inventory, saveData.playerInventory);

        technologyManager.setTechnologyPoints(saveData.technologyPoints);
        technologyManager.loadUnlockedBuildingNames(saveData.unlockedBuildings);

        setPlayerPositionAfterLoad(
            saveData.playerX,
            saveData.playerY,
            saveData.playerZ
        );

        clearPlacedBuildings();



        for (BuildingSaveData buildingData : saveData.buildings) {
            Vector3 position = new Vector3(
                buildingData.x,
                buildingData.y,
                buildingData.z
            );

            if (buildingData.type.equals("crafter")) {
                placeCrafter(position);
            }

            if (buildingData.type.equals("drill")) {
                placeDrill(position);

                if (drills.size > 0) {
                    Drill lastDrill = drills.peek();
                    saveManager.loadInventory(lastDrill.getInventory(), buildingData.inventory);
                }
            }

            if (buildingData.type.equals("excavator")) {
                placeExcavator(position);

                if (excavators.size > 0) {
                    Excavator lastExcavator = excavators.peek();
                    saveManager.loadInventory(lastExcavator.getOutputInventory(), buildingData.outputInventory);
                }
            }

            if (buildingData.type.equals("smelter")) {
                placeSmelter(position);

                if (smelters.size > 0) {
                    Smelter lastSmelter = smelters.peek();
                    saveManager.loadInventory(lastSmelter.getInputInventory(), buildingData.inputInventory);
                    saveManager.loadInventory(lastSmelter.getOutputInventory(), buildingData.outputInventory);
                }
            }

            if (buildingData.type.equals("cargo_container")) {
                placeCargoContainer(position);

                if (cargoContainers.size > 0) {
                    CargoContainer lastContainer = cargoContainers.peek();
                    saveManager.loadInventory(lastContainer.getInventory(), buildingData.inventory);
                }
            }

            if (buildingData.type.equals("vendor")) {
                placeVendor(position);
            }

            if (buildingData.type.equals("constructor")) {
                placeConstructor(position);

                if (constructors.size > 0) {
                    Constructor lastConstructor = constructors.peek();
                    saveManager.loadInventory(lastConstructor.getInputInventory(), buildingData.inputInventory);
                    saveManager.loadInventory(lastConstructor.getOutputInventory(), buildingData.outputInventory);
                }
            }
        }

        loadingGame = false;

        for (ConveyorSaveData conveyorData : saveData.conveyors) {
            OutputBuilding source = findSavedOutputBuilding(
                conveyorData.sourceType,
                conveyorData.sourceIndex
            );

            InputBuilding target = findSavedInputBuilding(
                conveyorData.targetType,
                conveyorData.targetIndex
            );

            if (source != null && target != null) {
                placeConveyor(source, target);
            }
        }


        inventoryUI.refresh();

        if (drillUI != null && drillUI.isOpen()) {
            drillUI.refresh();
        }

        if (excavatorUI != null && excavatorUI.isOpen()) {
            excavatorUI.refresh();
        }

        if (smelterUI != null && smelterUI.isOpen()) {
            smelterUI.refresh();
        }

        if (cargoContainerUI != null && cargoContainerUI.isOpen()) {
            cargoContainerUI.refresh();
        }

        System.out.println("Loaded save into world: " + saveName);
        return true;
    }

    private void saveGame(String saveName){
        saveManager.saveGame(
            saveName,
            inventory,
            currentPosition,
            crafters,
            drills,
            excavators,
            smelters,
            cargoContainers,
            conveyors,
            vendors,
            constructors,
            technologyManager
        );
    }

    private void clearPlacedBuildings() {
        for (Conveyor conveyor : conveyors) {
            sceneManager.removeScene(conveyor.getScene());
        }
        conveyors.clear();

        for (Crafter crafter : crafters) {
            sceneManager.removeScene(crafter.getScene());
        }
        crafters.clear();

        for (Drill drill : drills) {
            sceneManager.removeScene(drill.getScene());
        }
        drills.clear();

        for (Excavator excavator : excavators) {
            sceneManager.removeScene(excavator.getScene());
        }
        excavators.clear();

        for (Smelter smelter : smelters) {
            sceneManager.removeScene(smelter.getScene());
        }
        smelters.clear();

        for (Constructor constructor : constructors) {
            sceneManager.removeScene(constructor.getScene());
        }
        constructors.clear();

        for (CargoContainer container : cargoContainers) {
            sceneManager.removeScene(container.getScene());
        }
        cargoContainers.clear();

        for (Vendor vendor : vendors) {
            sceneManager.removeScene(vendor.getScene());
        }
        vendors.clear();

        hideBuildingPreview();
        hideDeletePreview();

        conveyorModeActive = false;
        selectedConveyorSource = null;
    }

    private boolean canPayBuildingCost(String itemId, int amount){
        return inventory.hasItem(itemId, amount);
    }

    private boolean payBuildingCost(String itemId, int amount){
        if (!canPayBuildingCost(itemId, amount)) {
            System.out.println("Nor enough resources. need"+ amount +"x " +itemId);
            return false;
        }

        inventory.removeItem(itemId, amount);

        if (inventoryUI != null) {
            inventoryUI.refresh();
        }

        return true;
    }


    private void processInput(float deltaTime) {

        // quickSave
        if(Gdx.input.isKeyJustPressed(Input.Keys.F5)){
            saveGame("quicksave");
        }

        // quickLoad
        if(Gdx.input.isKeyJustPressed(Input.Keys.F9)){
            loadGame("quicksave");
        }

        // menu
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if (crafterUI != null && crafterUI.isOpen()) {
                crafterUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if(drillUI != null && drillUI.isOpen()){
                drillUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (excavatorUI != null && excavatorUI.isOpen()) {
                excavatorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (smelterUI != null && smelterUI.isOpen()) {
                smelterUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (cargoContainerUI != null && cargoContainerUI.isOpen()) {
                cargoContainerUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (vendorUI != null && vendorUI.isOpen()) {
                vendorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (unlockMenu != null && unlockMenu.isOpen()) {
                unlockMenu.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (constructorUI != null && constructorUI.isOpen()) {
                constructorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if(menu.isOpen()){
                closeMenu();
            }else{
                openMenu();
            }
        }
        if (menu.isOpen()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            if (unlockMenu.isOpen()) {
                unlockMenu.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            } else {
                unlockMenu.open();
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(unlockMenu.getStage(), this, cameraController));
            }
        }

        // update player transform
        playerTransform.set(playerScene.modelInstance.transform);

        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            inventoryUI.toggle();

            if (inventoryUI.isOpen()) {
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(inventoryUI.getStage(), this, cameraController));
            } else {
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            float x = 525f;
            float z = 222f;
            float y = terrain.getHeightAtWorldCoord(x, z);

            currentPosition.set(x, y, z);
            playerScene.modelInstance.transform.setTranslation(currentPosition);

            System.out.println("Teleported to iron node.");
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.E)){
            if (crafterUI != null && crafterUI.isOpen()) {
                crafterUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (drillUI != null && drillUI.isOpen()) {
                drillUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (excavatorUI != null && excavatorUI.isOpen()) {
                excavatorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (cargoContainerUI != null && cargoContainerUI.isOpen()) {
                cargoContainerUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (smelterUI != null && smelterUI.isOpen()) {
                smelterUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (vendorUI != null && vendorUI.isOpen()) {
                vendorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            if (constructorUI != null && constructorUI.isOpen()) {
                constructorUI.close();
                Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
                return;
            }

            Constructor constructor = findNearestConstructor(10f);

            if (constructor != null) {
                constructorUI.open(constructor);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(constructorUI.getStage(), this, cameraController));
                return;
            }

            Vendor vendor = findNearestVendor(10f);

            if (vendor != null) {
                vendorUI.open(vendor);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(vendorUI.getStage(), this, cameraController));
                return;
            }

            CargoContainer container = findNearestCargoContainer(10f);

            if (container != null) {
                cargoContainerUI.open(container);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(cargoContainerUI.getStage(), this, cameraController));
                return;
            }

            Smelter smelter = findNearestSmelter(10f);

            if (smelter != null) {
                smelterUI.open(smelter);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(smelterUI.getStage(), this, cameraController));
                return;
            }

            Excavator excavator = findNearestExcavator(10f);

            if (excavator != null) {
                excavatorUI.open(excavator);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(excavatorUI.getStage(), this, cameraController));
                return;
            }

            Drill drill = findNearestDrill(10f);

            if(drill != null){
                drillUI.open(drill);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(drillUI.getStage(), this, cameraController));
                return;
            }


            Crafter crafter = findNearestCrafter(8f);

            if (crafter != null) {
                crafterUI.open(crafter);
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(new InputMultiplexer(crafterUI.getStage(), this, cameraController));
                return;
            }
        }

        if (inventoryUI.isOpen()) {
            return;
        }

        if (excavatorUI != null && excavatorUI.isOpen()) {
            return;
        }

        if (drillUI != null && drillUI.isOpen()) {
            return;
        }

        if (crafterUI != null && crafterUI.isOpen()) {
            return;
        }

        if (smelterUI != null && smelterUI.isOpen()) {
            return;
        }

        if (cargoContainerUI != null && cargoContainerUI.isOpen()) {
            return;
        }

        if (vendorUI != null && vendorUI.isOpen()) {
            return;
        }

        if (unlockMenu != null && unlockMenu.isOpen()) {
            return;
        }

        if (constructorUI != null && constructorUI.isOpen()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            inventory.addItem(new IronOre(), 1);
            inventoryUI.refresh();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectedBuildingType = 1;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            selectedBuildingType = 2;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            selectedBuildingType = 3;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            selectedBuildingType = 4;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            if (!technologyManager.isUnlocked(BuildingType.CONVEYOR)) {
                System.out.println("Conveyor is locked.");
                return;
            }

            conveyorModeActive = !conveyorModeActive;

            if (conveyorModeActive) {
                selectedBuildingType = 0;
                buildingPreviewActive = false;
                deleteModeActive = false;
                selectedConveyorSource = null;

                hideBuildingPreview();
                hideDeletePreview();

                System.out.println("Conveyor mode ON. Click output building.");
            } else {
                selectedConveyorSource = null;
                System.out.println("Conveyor mode OFF.");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            selectedBuildingType = 6;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
            selectedBuildingType = 7;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
            if (!technologyManager.isUnlocked(BuildingType.CONSTRUCTOR)) {
                System.out.println("Constructor is locked.");
                return;
            }

            selectedBuildingType = 8;
            buildingPreviewActive = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            selectedBuildingType = 0;
            hideBuildingPreview();
            closeDeleteMode();

            conveyorModeActive = false;
            selectedConveyorSource = null;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            deleteModeActive = !deleteModeActive;

            if (deleteModeActive) {
                selectedBuildingType = 0;
                buildingPreviewActive = false;
                previewBoxInstance.transform.setToTranslation(0,-1000,0);
            }else{
                hideDeletePreview();
            }
        }


        if(cameraMode != CameraMode.FLY_MODE){
            if(Gdx.input.isKeyPressed(Input.Keys.W)){
                moveTranslation.z += speed * deltaTime;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.S)){
                moveTranslation.z -= speed * deltaTime;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.A)){
                moveTranslation.x += speed * deltaTime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)){
                moveTranslation.x -= speed * deltaTime;
            }
            // jump
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
                verticalVelocity = jumpPower;
                isGrounded = false;
                playerScene.animationController.action("jump",1, 1f, this, 0.5f);
            }
            // sprint
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                speed = 20f;
            }
            if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                speed = 5f;
            }
            // player rotation
            float mouseDeltaX = Gdx.input.getDeltaX();
            float rotation = -mouseDeltaX * Settings.MOUSE_SENSITIVITY;

            playerTransform.rotate(Vector3.Y, rotation);
            angleBehindPlayer += rotation;

            if(Gdx.input.isKeyJustPressed(Input.Keys.W)){
                moveTranslation.z += speed * deltaTime;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.S)){
                moveTranslation.z -= speed * deltaTime;
            }
        }


        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
            switch (cameraMode) {
                case FREE_LOOK:
                    cameraMode = CameraMode.BEHIND_PLAYER;
                    angleAroundPlayer = angleBehindPlayer;
                    break;
                case BEHIND_PLAYER:
                    cameraMode = CameraMode.FLY_MODE;
                    break;
                case FLY_MODE:
                    cameraMode = CameraMode.FREE_LOOK;
                    break;
            }
        }

        // apply movement with building collision
        applyPlayerMovementWithCollision();

        // set modified transform
        playerScene.modelInstance.transform.set(playerTransform);

        // update vector position
        playerScene.modelInstance.transform.getTranslation(currentPosition);

        float height = terrain.getHeightAtWorldCoord(currentPosition.x, currentPosition.z);
//
        verticalVelocity += gravity*deltaTime;
        currentPosition.y += verticalVelocity * deltaTime;
//
        //currentPosition.y = height;

        if (currentPosition.y <= height){
           currentPosition.y = height;
           verticalVelocity = 0f;
           isGrounded = true;
        }

        // apply terrain height to player
        playerScene.modelInstance.transform.setTranslation(currentPosition);

        // clear move translation
        moveTranslation.set(0,0,0);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        sceneAsset.dispose();

        if (previewBoxModel != null) {
            previewBoxModel.dispose();
        }

        if (deletePreviewModel != null) {
            deletePreviewModel.dispose();
        }

        if (crafterUI != null) {
            crafterUI.dispose();
        }

        if (crafterModel != null) {
            crafterModel.dispose();
        }

        if (ironNodeModel != null) {
            ironNodeModel.dispose();
        }

        if(drillUI != null){
            drillUI.dispose();
        }

        if(drillModel != null){
            drillModel.dispose();
        }

        if(excavatorUI != null){
            excavatorUI.dispose();
        }

        if(excavatorModel != null){
            excavatorModel.dispose();
        }

        if (smelterUI != null) {
            smelterUI.dispose();
        }

        if (smelterModel != null) {
            smelterModel.dispose();
        }

        if(conveyorModel != null){
            conveyorModel.dispose();
        }

        if (cargoContainerUI != null) {
            cargoContainerUI.dispose();
        }

        if (cargoContainerModel != null) {
            cargoContainerModel.dispose();
        }

        if (constructorUI != null) {
            constructorUI.dispose();
        }

        if (constructorModel != null) {
            constructorModel.dispose();
        }

        if (hoverStage != null) {
            hoverStage.dispose();
        }

        if (hoverSkin != null) {
            hoverSkin.dispose();
        }

        if(menu != null){
            menu.dispose();
        }

        if(inventoryUI != null){
            inventoryUI.dispose();
        }

        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();

    }

    @Override
    public void onEnd(AnimationController.AnimationDesc animation) {

    }

    @Override
    public void onLoop(AnimationController.AnimationDesc animation) {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT && deleteModeActive) {
            if (getMouseWorldPosition(screenX, screenY, pickedPosition)) {
                if (findDeleteTargetAt(pickedPosition)) {
                    removeDeleteTarget();
                } else {
                    System.out.println("No building selected for delete.");
                }
            }

            return true;
        }

        if (button == Input.Buttons.LEFT && conveyorModeActive) {
            if (getMouseWorldPosition(screenX, screenY, pickedPosition)) {

                if (selectedConveyorSource == null) {
                    selectedConveyorSource = findOutputBuildingAt(pickedPosition);

                    if (selectedConveyorSource != null) {
                        System.out.println("Selected conveyor source. Click input building.");
                    } else {
                        System.out.println("No output building selected.");
                    }

                    return true;
                }

                InputBuilding target = findInputBuildingAt(pickedPosition);

                if (target != null) {
                    placeConveyor(selectedConveyorSource, target);
                    selectedConveyorSource = null;
                    conveyorModeActive = false;
                } else {
                    System.out.println("No input building selected.");
                }
            }

            return true;
        }

        if (button == Input.Buttons.LEFT && buildingPreviewActive){
            if(getMouseWorldPosition(screenX, screenY, pickedPosition)){

                if (selectedBuildingType == 1){
                    placeCrafter(pickedPosition);
                }
                if (selectedBuildingType == 2){
                    placeDrill(pickedPosition);
                }
                if (selectedBuildingType == 3){
                    placeExcavator(pickedPosition);
                }

                if (selectedBuildingType == 4){
                    placeSmelter(pickedPosition);
                }

                if (selectedBuildingType == 6) {
                    placeCargoContainer(pickedPosition);
                }

                if (selectedBuildingType == 7) {
                    placeVendor(pickedPosition);
                }

                if (selectedBuildingType == 8) {
                    placeConstructor(pickedPosition);
                }
            }
            return true;
        }

        if (button == Input.Buttons.LEFT) {
            if (menu.isOpen()
                || inventoryUI.isOpen()
                || drillUI.isOpen()
                || crafterUI.isOpen()
                || excavatorUI.isOpen()
                || smelterUI.isOpen()
                || cargoContainerUI.isOpen()
                || vendorUI.isOpen()
                || unlockMenu.isOpen()) {
                return false;
            }

            if (getMouseWorldPosition(screenX, screenY, pickedPosition)) {
                ResourceNode node = findResourceNodeAt(pickedPosition);

                if (node != null) {
                    manuallyMineNode(node);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float zoomLevel = amountY * Settings.CAMERA_ZOOM_LEVEL_FACTOR;
        distanceFromPlayer += zoomLevel;
        if (distanceFromPlayer < Settings.CAMERA_MIN_PITCH) {
            distanceFromPlayer = Settings.CAMERA_MIN_PITCH;
        }
        if (distanceFromPlayer > Settings.CAMERA_MAX_PITCH) {
            distanceFromPlayer = Settings.CAMERA_MAX_PITCH;
        }
        return false;
    }


}
