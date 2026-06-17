package io.github.bartek813.test_dwa.saves;


import com.badlogic.gdx.utils.Array;

public class BuildingSaveData {
    public String type;

    public float x;
    public float y;
    public float z;

    public String resourceNodeId;

    public Array<ItemStackSaveData> inventory = new Array<>();

    public Array<ItemStackSaveData> inputInventory = new Array<>();
    public Array<ItemStackSaveData> outputInventory = new Array<>();

    public BuildingSaveData(){

    }
}
