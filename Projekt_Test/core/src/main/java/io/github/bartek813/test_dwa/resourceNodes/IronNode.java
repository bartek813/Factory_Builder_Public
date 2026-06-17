package io.github.bartek813.test_dwa.resourceNodes;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;

public class IronNode extends ResourceNode {
    public IronNode(Model model, Vector3 position) {
        super(
            "iron_ore",
            "Iron Node",
            model,
            position,
            8f,
            2f,
            8f
        );
    }
}
