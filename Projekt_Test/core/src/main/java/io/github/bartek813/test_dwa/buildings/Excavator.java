package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.bartek813.test_dwa.enums.PortType;
import io.github.bartek813.test_dwa.items.Item;
import io.github.bartek813.test_dwa.items.ItemFactory;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.resourceNodes.ResourceNode;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Excavator implements OutputBuilding{
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final ResourceNode resourceNode;
    private final Inventory outputInventory;

    private final Array<BuildingPort> inputPorts = new Array<>();
    private final Array<BuildingPort> outputPorts = new Array<>();

    private final Vector3 basePosition = new Vector3();

    private final float width;
    private final float height;
    private final float depth;

    private float miningTimer = 0f;
    private final float MiningTime = 2f;

    public Excavator(
        Model model,
        Vector3 position,
        float width,
        float height,
        float depth,
        ResourceNode resourceNode
    ) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.resourceNode = resourceNode;

        this.outputInventory = new Inventory(3);

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

        outputPorts.add(new BuildingPort(
            PortType.OUTPUT,
            new Vector3(0f,0f,depth / 2f + 1f)
        ));
    }

    public void update(float deltaTime){
        miningTimer += deltaTime;

        if(miningTimer >= MiningTime){
            miningTimer = 0;
            mineResource();
        }
    }

    private void mineResource(){
        Item item = ItemFactory.createItem(resourceNode.getResourceItemId());

        int remaining = outputInventory.addItem(item, 1);

    }

    @Override
    public boolean collidesWith(Rectangle otherBounds){
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


    public Inventory getOutputInventory() {
        return outputInventory;
    }


    public ResourceNode getResourceNode() {
        return resourceNode;
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
    public Vector3 getPosition(Vector3 out) {
        modelInstance.transform.getTranslation(out);
        return out;
    }

    @Override
    public Vector3 getBasePosition(Vector3 out) {
        return out.set(basePosition);
    }

}
