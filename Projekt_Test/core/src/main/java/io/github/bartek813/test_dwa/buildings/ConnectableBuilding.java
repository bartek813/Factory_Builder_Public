package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.utils.Array;

public interface ConnectableBuilding extends Building{
    Array<BuildingPort> getInputPorts();

    Array<BuildingPort> getOutputPorts();
}
