package io.github.bartek813.test_dwa.saves;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import io.github.bartek813.test_dwa.buildings.*;
import io.github.bartek813.test_dwa.items.Item;
import io.github.bartek813.test_dwa.items.ItemFactory;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import io.github.bartek813.test_dwa.resourceNodes.ResourceNode;
import io.github.bartek813.test_dwa.technology.TechnologyManager;

public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String LAST_SAVE_FILE = "savegame.json";

    private final Json json = new Json();
    private final Vector3 tempPosition = new Vector3();

    public void saveGame(
        String saveName,
        Inventory playerInventory,
        Vector3 playerPosition,
        Array<Crafter> crafters,
        Array<Drill> drills,
        Array<Excavator> excavators,
        Array<Smelter> smelters,
        Array<CargoContainer> cargoContainers,
        Array<Conveyor> conveyors,
        Array<Vendor> vendors,
        Array<Constructor> constructors,
        TechnologyManager technologyManager
    ) {
        SaveData saveData = new SaveData();

        saveData.playerX = playerPosition.x;
        saveData.playerY = playerPosition.y;
        saveData.playerZ = playerPosition.z;

        saveData.playerInventory = saveInventory(playerInventory);

        saveData.technologyPoints = technologyManager.getTechnologyPoints();
        saveData.unlockedBuildings = technologyManager.getUnlockedBuildingNames();

        saveCrafters(saveData, crafters);
        saveDrills(saveData, drills);
        saveExcavators(saveData, excavators);
        saveSmelters(saveData, smelters);
        saveCargoContainers(saveData, cargoContainers);
        saveConveyors(saveData, conveyors, excavators, smelters, cargoContainers, constructors);
        saveVendors(saveData, vendors);
        saveConstructors(saveData, constructors);

        FileHandle file = getSaveFile(saveName);
        file.writeString(json.prettyPrint(saveData), false);

        setLastSaveName(saveName);

        System.out.println("Game saved: " + saveName);
    }

    public SaveData loadGame(String saveName){
        String safeName = sanitizeSaveName(saveName);

        FileHandle file = getSaveFile(safeName);

        if (!file.exists()){
            System.out.println("No save found" + safeName);
            return null;
        }

        SaveData saveData = json.fromJson(SaveData.class, file.readString());

        setLastSaveName(safeName);

        System.out.println("Game loaded" + safeName);
        return saveData;
    }

    public Array<String> getSaveNames(){
        Array<String> saveNames = new Array<>();

        FileHandle dir = Gdx.files.local(SAVE_DIR);

        if(!dir.exists()){
            dir.mkdirs();
            return saveNames;
        }

        FileHandle[] files = dir.list();

        for (FileHandle file : files) {
            if (file.extension().equals("json")) {
                saveNames.add(file.nameWithoutExtension());
            }
        }

        return saveNames;
    }

    public String getLastSaveName(){
        FileHandle file = Gdx.files.local(LAST_SAVE_FILE);

        if (!file.exists()){
            return "quicksave";
        }

        String name = file.readString().trim();

        if(name.length() == 0){
            return "quicksave";
        }

        return name;
    }

    private void setLastSaveName(String saveName) {
        Gdx.files.local(LAST_SAVE_FILE).writeString(saveName, false);
    }

    private FileHandle getSaveFile(String saveName){
        FileHandle dir = Gdx.files.local(SAVE_DIR);

        if(!dir.exists()){
            dir.mkdirs();
        }

        return dir.child(saveName + ".json");
    }

    private String sanitizeSaveName(String saveName){
        if(saveName == null || saveName.length() == 0){
            return "quicksave";
        }

        return saveName
            .trim()
            .replace(" ", "_")
            .replace("/", "_")
            .replace("\\", "_")
            .replace(":", "_");
    }

    private Array<ItemStackSaveData> saveInventory(Inventory inventory) {
        Array<ItemStackSaveData> output = new Array<>();

        for (InventorySlot slot : inventory.getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            output.add(new ItemStackSaveData(
                slot.getItem().getId(),
                slot.getAmount()
            ));
        }

        return output;
    }

    private void saveCrafters(SaveData saveData, Array<Crafter> crafters) {
        Vector3 position = new Vector3();

        for (Crafter crafter : crafters) {
            crafter.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "crafter";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.inputInventory = saveInventory(crafter.getInputInventory());
            buildingData.outputInventory = saveInventory(crafter.getOutputInventory());

            saveData.buildings.add(buildingData);
        }
    }

    private void saveDrills(SaveData saveData, Array<Drill> drills) {
        Vector3 position = new Vector3();

        for (Drill drill : drills) {
            drill.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "drill";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.inventory = saveInventory(drill.getInventory());

            saveData.buildings.add(buildingData);
        }
    }

    private void saveExcavators(SaveData saveData, Array<Excavator> excavators) {
        Vector3 position = new Vector3();

        for (Excavator excavator : excavators) {
            excavator.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "excavator";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.outputInventory = saveInventory(excavator.getOutputInventory());

            saveData.buildings.add(buildingData);
        }
    }

    private void saveSmelters(SaveData saveData, Array<Smelter> smelters) {
        Vector3 position = new Vector3();

        for (Smelter smelter : smelters) {
            smelter.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "smelter";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.inputInventory = saveInventory(smelter.getInputInventory());
            buildingData.outputInventory = saveInventory(smelter.getOutputInventory());

            saveData.buildings.add(buildingData);
        }
    }

    private void saveCargoContainers(SaveData saveData, Array<CargoContainer> cargoContainers) {
        Vector3 position = new Vector3();

        for (CargoContainer container : cargoContainers) {
            container.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "cargo_container";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.inventory = saveInventory(container.getInventory());

            saveData.buildings.add(buildingData);
        }
    }

    private void saveConveyors(
        SaveData saveData,
        Array<Conveyor> conveyors,
        Array<Excavator> excavators,
        Array<Smelter> smelters,
        Array<CargoContainer> cargoContainers,
        Array<Constructor> constructors
    ) {
        for (Conveyor conveyor : conveyors) {
            OutputBuilding source = conveyor.getSource();
            InputBuilding target = conveyor.getTarget();

            ConveyorSaveData conveyorData = new ConveyorSaveData();

            if (source instanceof Excavator) {
                conveyorData.sourceType = "excavator";
                conveyorData.sourceIndex = excavators.indexOf((Excavator) source, true);
            } else if (source instanceof Smelter) {
                conveyorData.sourceType = "smelter";
                conveyorData.sourceIndex = smelters.indexOf((Smelter) source, true);
            } else if (source instanceof CargoContainer) {
                conveyorData.sourceType = "cargo_container";
                conveyorData.sourceIndex = cargoContainers.indexOf((CargoContainer) source, true);
            } else if (source instanceof Constructor) {
                conveyorData.sourceType = "constructor";
                conveyorData.sourceIndex = constructors.indexOf((Constructor) source, true);
            } else {
                continue;
            }

            if (target instanceof Smelter) {
                conveyorData.targetType = "smelter";
                conveyorData.targetIndex = smelters.indexOf((Smelter) target, true);
            } else if (target instanceof CargoContainer) {
                conveyorData.targetType = "cargo_container";
                conveyorData.targetIndex = cargoContainers.indexOf((CargoContainer) target, true);
            } else if (target instanceof Constructor) {
                conveyorData.targetType = "constructor";
                conveyorData.targetIndex = constructors.indexOf((Constructor) target, true);
            }else {
                continue;
            }

            if (conveyorData.sourceIndex < 0 || conveyorData.targetIndex < 0) {
                continue;
            }

            saveData.conveyors.add(conveyorData);
        }
    }

    private void saveVendors(SaveData saveData, Array<Vendor> vendors) {
        Vector3 position = new Vector3();

        for (Vendor vendor : vendors) {
            vendor.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "vendor";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            saveData.buildings.add(buildingData);
        }
    }

    private void saveConstructors(SaveData saveData, Array<Constructor> constructors) {
        Vector3 position = new Vector3();

        for (Constructor constructor : constructors) {
            constructor.getBasePosition(position);

            BuildingSaveData buildingData = new BuildingSaveData();
            buildingData.type = "constructor";
            buildingData.x = position.x;
            buildingData.y = position.y;
            buildingData.z = position.z;

            buildingData.inputInventory = saveInventory(constructor.getInputInventory());
            buildingData.outputInventory = saveInventory(constructor.getOutputInventory());

            saveData.buildings.add(buildingData);
        }
    }

    public void loadInventory(Inventory inventory, Array<ItemStackSaveData> savedItems){
        inventory.clear();

        for (ItemStackSaveData savedItem : savedItems){
            Item item = ItemFactory.createItem(savedItem.itemId);
            inventory.addItem(item, savedItem.amount);
        }
    }

}
