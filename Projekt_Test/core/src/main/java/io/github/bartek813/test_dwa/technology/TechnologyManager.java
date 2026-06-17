package io.github.bartek813.test_dwa.technology;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.bartek813.test_dwa.enums.BuildingType;

public class TechnologyManager {
    private int technologyPoints = 0;

    private final ObjectMap<BuildingType, Boolean> unlockedBuildings = new ObjectMap<>();
    private final ObjectMap<BuildingType, Integer> unlockCosts = new ObjectMap<>();

    public TechnologyManager() {
        setupUnlockCosts();
        setupStartingUnlocks();
    }

    private void setupUnlockCosts() {
        unlockCosts.put(BuildingType.DRILL, 10);
        unlockCosts.put(BuildingType.EXCAVATOR, 20);
        unlockCosts.put(BuildingType.SMELTER, 25);
        unlockCosts.put(BuildingType.CARGO_CONTAINER, 15);
        unlockCosts.put(BuildingType.CONVEYOR, 30);
        unlockCosts.put(BuildingType.CONSTRUCTOR, 35);
    }

    private void setupStartingUnlocks() {
        unlockedBuildings.put(BuildingType.CRAFTER, true);
        unlockedBuildings.put(BuildingType.VENDOR, true);

        unlockedBuildings.put(BuildingType.DRILL, false);
        unlockedBuildings.put(BuildingType.EXCAVATOR, false);
        unlockedBuildings.put(BuildingType.SMELTER, false);
        unlockedBuildings.put(BuildingType.CARGO_CONTAINER, false);
        unlockedBuildings.put(BuildingType.CONVEYOR, false);
        unlockedBuildings.put(BuildingType.CONSTRUCTOR, false);
    }

    public boolean isUnlocked(BuildingType type) {
        Boolean unlocked = unlockedBuildings.get(type);
        return unlocked != null && unlocked;
    }

    public boolean unlock(BuildingType type) {
        if (isUnlocked(type)) {
            return false;
        }

        int cost = getUnlockCost(type);

        if (technologyPoints < cost) {
            return false;
        }

        technologyPoints -= cost;
        unlockedBuildings.put(type, true);

        return true;
    }

    public int getUnlockCost(BuildingType type) {
        Integer cost = unlockCosts.get(type);
        return cost == null ? 0 : cost;
    }

    public void addTechnologyPoints(int amount) {
        if (amount <= 0) {
            return;
        }

        technologyPoints += amount;
    }

    public Array<String> getUnlockedBuildingNames() {
        Array<String> result = new Array<>();

        for (BuildingType type : BuildingType.values()) {
            if (isUnlocked(type)) {
                result.add(type.name());
            }
        }

        return result;
    }

    public void loadUnlockedBuildingNames(Array<String> unlockedNames) {
        setupStartingUnlocks();

        if (unlockedNames == null) {
            return;
        }

        for (String name : unlockedNames) {
            try {
                BuildingType type = BuildingType.valueOf(name);
                setUnlocked(type, true);
            } catch (Exception e) {
                System.out.println("Unknown unlocked building type in save: " + name);
            }
        }
    }

    public int getTechnologyPoints() {
        return technologyPoints;
    }

    public void setTechnologyPoints(int technologyPoints) {
        this.technologyPoints = Math.max(0, technologyPoints);
    }

    public ObjectMap<BuildingType, Boolean> getUnlockedBuildings() {
        return unlockedBuildings;
    }

    public void setUnlocked(BuildingType type, boolean unlocked) {
        unlockedBuildings.put(type, unlocked);
    }
}
