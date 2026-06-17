package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.math.Vector3;
import io.github.bartek813.test_dwa.enums.PortType;

public class BuildingPort {
    private final PortType type;
    private final Vector3 localPosition;

    public BuildingPort(PortType type, Vector3 localPosition){
        this.type = type;
        this.localPosition = new Vector3(localPosition);
    }

    public PortType getType() {
        return type;
    }

    public Vector3 getLocalPosition() {
        return localPosition;
    }

    public Vector3 getWorldPosition(Vector3 buildingBasePosition, Vector3 out){
        return out.set(buildingBasePosition).add(localPosition);
    }

}
