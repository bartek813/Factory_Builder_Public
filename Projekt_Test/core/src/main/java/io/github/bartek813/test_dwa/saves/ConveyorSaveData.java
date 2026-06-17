package io.github.bartek813.test_dwa.saves;

public class ConveyorSaveData {
    public int sourceIndex;
    public String sourceType;

    public int targetIndex;
    public String targetType;

    public ConveyorSaveData(){
    }

    public ConveyorSaveData(String sourceType, int sourceIndex, String targetType, int targetIndex) {
        this.sourceType = sourceType;
        this.sourceIndex = sourceIndex;
        this.targetType = targetType;
        this.targetIndex = targetIndex;
    }

}
