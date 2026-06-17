package io.github.bartek813.test_dwa.saves;

import com.badlogic.gdx.utils.Array;

public class SaveData {
    public float playerX;
    public float playerY;
    public float playerZ;

    public Array<ItemStackSaveData> playerInventory = new Array<>();
    public Array<BuildingSaveData> buildings = new Array<>();
    public Array<ConveyorSaveData> conveyors = new Array<>();

    public int technologyPoints = 0;
    public Array<String> unlockedBuildings = new Array<>();

    public SaveData(){
    }
}


