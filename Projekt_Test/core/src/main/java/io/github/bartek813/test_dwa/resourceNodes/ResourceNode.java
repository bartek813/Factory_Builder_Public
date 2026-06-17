package io.github.bartek813.test_dwa.resourceNodes;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

public class ResourceNode {
    private final String resourceItemId;
    private final String displayName;

    private final ModelInstance modelInstance;
    private final Scene scene;
    private final Rectangle bounds;

    private final float width;
    private final float height;
    private final float depth;

    public ResourceNode(
        String resourceItemId,
        String displayName,
        Model model,
        Vector3 position,
        float width,
        float height,
        float depth
    ) {
        this.resourceItemId = resourceItemId;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.depth = depth;

        modelInstance = new ModelInstance(model);

        modelInstance.transform.setToTranslation(
            position.x,
            position.y + height / 2f,
            position.z
        );

        scene = new Scene(modelInstance);

        bounds = new Rectangle(
            position.x - width / 2f,
            position.z -depth / 2f,
            width,
            depth
        );
    }

    public String getResourceItemId() {
        return resourceItemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Scene getScene() {
        return scene;
    }

    public boolean contains(Vector3 position) {
        return bounds.contains(position.x, position.y);
    }

    public boolean overlaps(Rectangle otherBounds) {
        return bounds.overlaps(otherBounds);
    }

    public Vector3 getPosition(Vector3 out) {
        modelInstance.transform.getTranslation(out);
        return out;
    }

}
