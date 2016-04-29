package nz.sodium.ftack;

import javaslang.collection.Array;

public class Scene {
    public Scene(
    		Array<Block> blocks,
    		float zCentre,
    		float zoomLevel) {
        this.blocks = blocks;
        this.zCentre = zCentre;
        this.zoomLevel = zoomLevel;
    }
    public final Array<Block> blocks;
    public final float zCentre;
    public final float zoomLevel;
    public final Scene zoom(float zCentre, float zoomLevel) {
    	return new Scene(blocks, zCentre, zoomLevel);
    }
}
