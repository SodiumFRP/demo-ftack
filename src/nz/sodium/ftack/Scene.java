package nz.sodium.ftack;

import javaslang.collection.Array;

public class Scene {
    public Scene(
    		Array<Block> blocks,
    		float zCentre,
    		float zoom) {
        this.blocks = blocks;
        this.zCentre = zCentre;
        this.zoom = zoom;
    }
    public final Array<Block> blocks;
    public final float zCentre;
    public final float zoom;
}
