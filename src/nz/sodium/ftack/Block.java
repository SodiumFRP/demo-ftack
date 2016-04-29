package nz.sodium.ftack;

import nz.sodium.Tuple2;
import java.util.Optional;


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
    public final Block setColour(Colour colour) {
    	return new Block(p0, p1, colour);
    }
    public final Block translate(float x, float y, float z) {
    	return new Block(p0.translate(x, y, z), p1.translate(x, y, z), colour);
    }
    public final Optional<Tuple2<Block, Block>> chopX(float x) {
        return x <= p0.x || x >= p1.x ? Optional.empty()
        		: Optional.of(new Tuple2<Block, Block>(
        			new Block(new Point(p0.x, p0.y, p0.z),
        					  new Point(x, p1.y, p1.z), colour),
        			new Block(new Point(x, p0.y, p0.z),
        					  new Point(p1.x, p1.y, p1.z), colour)
				  ));
    }
    public final Optional<Tuple2<Block, Block>> chopY(float y) {
        return y <= p0.y || y >= p1.y ? Optional.empty()
        		: Optional.of(new Tuple2<Block, Block>(
        			new Block(new Point(p0.x, p0.y, p0.z),
        					  new Point(p1.x, y, p1.z), colour),
        			new Block(new Point(p0.x, y, p0.z),
        					  new Point(p1.x, p1.y, p1.z), colour)
				  ));
    }
}
