package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.bartek813.test_dwa.enums.PortType;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import net.mgsx.gltf.scene3d.scene.Scene;

public class CargoContainer implements OutputBuilding, InputBuilding{
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final Inventory inventory;

    private final Array<BuildingPort> inputPorts = new Array<>();
    private final Array<BuildingPort> outputPorts = new Array<>();

    private final Vector3 basePosition = new Vector3();

    private final float width;
    private final float height;
    private final float depth;

    public CargoContainer(Model model, Vector3 position, float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        inventory = new Inventory(8);

        basePosition.set(position);

        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(
            position.x,
            position.y + height / 2f,
            position.z
        );

        scene = new Scene(modelInstance);

        collisionBounds = new Rectangle(
            position.x - width / 2f,
            position.z - depth / 2f,
            width,
            depth
        );

        inputPorts.add(new BuildingPort(
            PortType.INPUT,
            new Vector3(-width / 2f - 1f, 0f, 0f)
        ));

        outputPorts.add(new BuildingPort(
            PortType.OUTPUT,
            new Vector3(width / 2f + 1f, 0f, 0f)
        ));
    }

    @Override
    public Inventory getInputInventory() {
        return inventory;
    }

    @Override
    public Inventory getOutputInventory() {
        return inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Array<BuildingPort> getInputPorts() {
        return inputPorts;
    }

    @Override
    public Array<BuildingPort> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public boolean collidesWith(Rectangle otherBounds) {
        return collisionBounds.overlaps(otherBounds);
    }

    @Override
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public Vector3 getPosition(Vector3 out) {
        modelInstance.transform.getTranslation(out);
        return out;
    }

    @Override
    public Vector3 getBasePosition(Vector3 out) {
        return out.set(basePosition);
    }
}
