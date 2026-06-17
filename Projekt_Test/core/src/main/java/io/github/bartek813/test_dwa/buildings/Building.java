package io.github.bartek813.test_dwa.buildings;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

public interface Building {
    Scene getScene();

    Rectangle getCollisionBounds();

    Vector3 getPosition(Vector3 out);

    Vector3 getBasePosition(Vector3 out);

    boolean collidesWith(Rectangle otherBounds);
}
