package io.github.bartek813.test_dwa.terrains;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public abstract class Terrain implements Disposable {
    protected int size;
    protected int width;
    protected float heightMagnitude;

    protected ModelInstance modelInstance;

    public ModelInstance getModelInstance(){
        return modelInstance;
    }

    abstract public float getHeightAtWorldCoord(float worldX, float worldZ);
}
