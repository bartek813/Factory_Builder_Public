package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Vendor implements Building {
    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle collisionBounds;

    private final Vector3 basePosition = new Vector3();

    private final float width;
    private final float height;
    private final float depth;

    public Vendor(Model model, Vector3 position, float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

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
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public Rectangle getCollisionBounds() {
        return collisionBounds;
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

    @Override
    public boolean collidesWith(Rectangle otherBounds) {
        return collisionBounds.overlaps(otherBounds);
    }
}
