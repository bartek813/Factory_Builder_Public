package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.bartek813.test_dwa.menus.inventory.Inventory;
import io.github.bartek813.test_dwa.menus.inventory.InventorySlot;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Conveyor {
    private final OutputBuilding source;
    private final InputBuilding target;

    private final ModelInstance modelInstance;
    private final Scene scene;

    private final Vector3 startPosition = new Vector3();
    private final Vector3 endPosition = new Vector3();
    private final Vector3 middlePosition = new Vector3();
    private final Vector3 direction = new Vector3();

    private float transferTimer = 0f;
    private final float transferTime = 0.5f;

    public Conveyor(Model model, OutputBuilding source, InputBuilding target) {
        this.source = source;
        this.target = target;

        modelInstance = new ModelInstance(model);
        scene = new Scene(modelInstance);

        updateVisualTransform();
    }

    private void updateVisualTransform() {
        Vector3 sourceBase = new Vector3();
        Vector3 targetBase = new Vector3();

        source.getBasePosition(sourceBase);
        target.getBasePosition(targetBase);

        if (source.getOutputPorts().size > 0) {
            source.getOutputPorts().first().getWorldPosition(sourceBase, startPosition);
        } else {
            startPosition.set(sourceBase);
        }

        if (target.getInputPorts().size > 0) {
            target.getInputPorts().first().getWorldPosition(targetBase, endPosition);
        } else {
            endPosition.set(targetBase);
        }

        startPosition.y += 0.6f;
        endPosition.y += 0.6f;

        middlePosition.set(startPosition).add(endPosition).scl(0.5f);

        direction.set(endPosition).sub(startPosition);

        float length = (float) Math.sqrt(
            direction.x * direction.x +
                direction.z * direction.z
        );

        if (length <= 0.01f) {
            length = 1f;
        }

        float angle = MathUtils.atan2(direction.z, direction.x) * MathUtils.radiansToDegrees;

        modelInstance.transform.idt();

        modelInstance.transform.setToTranslation(
            middlePosition.x,
            middlePosition.y,
            middlePosition.z
        );

        modelInstance.transform.rotate(Vector3.Y, -angle);

        modelInstance.transform.scale(
            length,
            0.3f,
            1.2f
        );
    }

    public void update(float deltaTime){
        transferTimer += deltaTime;

        if(transferTimer < transferTime){
            return;
        }

        transferTimer = 0f;
        transferOneItem();
    }

    private void transferOneItem(){
        Inventory sourceInventory = source.getOutputInventory();
        Inventory targetInventory = target.getInputInventory();

        for( InventorySlot sourceSlot : sourceInventory.getSlots() ){
            if (sourceSlot.isEmpty()){
                continue;
            }

            int remaining = targetInventory.addItem(sourceSlot.getItem(), 1);

            if (remaining == 0){
                sourceSlot.remove(1);
            }

            return;
        }
    }

    public OutputBuilding getSource() {
        return source;
    }

    public InputBuilding getTarget() {
        return target;
    }

    public Scene getScene(){
        return scene;
    }

}
