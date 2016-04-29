package nz.sodium.ftack;

public class Point {
    public Point(float x, float y, float z) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }
    public final float x;
    public final float y;
    public final float z;
    public final Point translate(float vx, float vy, float vz) {
    	return new Point(x+vx, y+vy, z+vz);
    }
}
