package nz.sodium.ftack;

import java.util.Optional;

import javaslang.collection.Array;
import nz.sodium.*;
import nz.sodium.time.*;


public class Match {

	private static final float VERTICAL_BLOCK_HEIGHT = 1f;
	private static final float VERTICAL_SCROLL_VELOCITY = 3f;
	private static final int BENCHMARK_SIZE = 10;
	private static final int HORIZONTAL_SLIDE_VELOCITY = 4;

	public static final Block initialBlock =
		new Block(new Point(-BENCHMARK_SIZE/2, -BENCHMARK_SIZE/2, 0),
				  new Point(BENCHMARK_SIZE/2, BENCHMARK_SIZE/2, VERTICAL_BLOCK_HEIGHT),
				  Match.levelColour(0));

	public static final Array<Block> initialStack =
		Array.of(initialBlock);

	public static final Scene initialScene =
        new Scene(initialStack, 0, 1);

	public static Colour levelColour(int level) {
		return Colour.fromHSV((float)level * 6,0.4f,1.0f);
	}

	public Match(TimerSystem<Double> sys, Stream<Unit> sClick)
	{
		Cell<Double> time = sys.time;
		double t0 = time.sample();
        scene = time.map(t -> {
    			float disp = BENCHMARK_SIZE
    					- (float)(t - t0)
    					* HORIZONTAL_SLIDE_VELOCITY;
    			Block blk = initialBlock
    					.translate(disp, 0, 0);
        		return new Scene(Array.of(blk), 0, 1);
        	});
        sGameOver = sClick;
	}

	public final Cell<Scene> scene;
	public final Stream<Unit> sGameOver;
}
