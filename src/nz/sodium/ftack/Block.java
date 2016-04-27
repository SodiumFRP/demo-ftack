package nz.sodium.ftack;

public class Block {
    public Block(Point p0, Point p1, Colour colour) {
    	this.p0 = p0;
    	this.p1 = p1;
    	this.colour = colour;
    }
    public final Point p0;
    public final Point p1;
    public final Colour colour;
    public final float width() { return p1.x - p0.x; }
    public final float depth() { return p1.y - p0.y; }
    public final float height() { return p1.z - p0.z; }
    public final Point centre() {
    	return new Point((p0.x + p1.x) * 0.5f,
    			         (p0.y + p1.y) * 0.5f,
    			         (p0.z + p1.z) * 0.5f);
    }
}
