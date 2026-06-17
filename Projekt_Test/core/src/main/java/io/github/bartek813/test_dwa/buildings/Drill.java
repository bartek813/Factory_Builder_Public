package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import io.github.bartek813.test_dwa.items.IronOre;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.resourceNodes.ResourceNode;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Drill implements Building{
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final ResourceNode resourceNode;
    private final Inventory inventory;

    private final float width;
    private final float height;
    private final float depth;

    private float miningTimer = 0f;
    private final float miningTime = 2f;

    private final Vector3 basePosition = new Vector3();

    public Drill(
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

        this.inventory = new Inventory(3);

        modelInstance = new ModelInstance(model);

        modelInstance.transform.setToTranslation(
            position.x,
            position.y + height / 2f,
            position.z
        );

        scene = new Scene(modelInstance);

        basePosition.set(position);

        collisionBounds = new Rectangle(
            position.x - width / 2f,
            position.z - depth / 2f,
            width,
            depth
        );
    }

    public void update(float deltaTime){
        miningTimer += deltaTime;

        if (miningTimer >= miningTime){
            miningTimer = 0f;
            mineResource();
        }
    }

    private void mineResource(){
        if (resourceNode.getResourceItemId().equals("iron_ore")){
            int remaining = inventory.addItem(new IronOre(), 1);

//            if (remaining == 0){
//                System.out.println("Drill minde 1 Iron ore");
//            } else {
//                System.out.println("Drill is full");
//            }
        }
    }

    public boolean collidesWith(Rectangle otherBounds){
        return collisionBounds.overlaps(otherBounds);
    }

    public Rectangle getCollisionBounds(){
        return collisionBounds;
    }

    public Scene getScene(){
        return scene;
    }

    public Inventory getInventory(){
        return inventory;
    }

    public ResourceNode getResourceNode(){
        return resourceNode;
    }

    public Vector3 getPosition(Vector3 out){
        modelInstance.transform.getTranslation(out);
        return out;
    }

    public Vector3 getBasePosition(Vector3 out){

        return out.set(basePosition);
    }
}
