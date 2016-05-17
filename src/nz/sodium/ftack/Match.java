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
		Cell<Boolean> direction = sClick.accum(false, (u, d) -> !d);
	    CellLoop<Block> block = new CellLoop<>();
		double t0 = sys.time.sample();
        Cell<Double> tLevel = sClick.snapshot(sys.time).hold(t0);
		Cell<Float> displacement = sys.time.lift(tLevel,
				(t, tLev) -> BENCHMARK_SIZE - (float)(t - tLev) * HORIZONTAL_SLIDE_VELOCITY);
		CellLoop<Array<Block>> stack = new CellLoop<>();
        Stream<Optional<Array<Block>>> sUpdate =
        		sClick.snapshot(stack, block, displacement, direction,
        				(u, s, blk, disp, dir) -> {
        			int level = s.length();
        			Block below = s.get(level-1);
        			Optional<Block> oBlk = blk.overlap(below, disp, dir);
        			if (oBlk.isPresent())
	        		    return Optional.of(s.append(oBlk.get()));
        			else
        				return Optional.empty();
        		});
        stack.loop(Stream.filterOptional(sUpdate).hold(initialStack));
        block.loop(displacement.lift(stack, direction,
        		(disp, s, dir) -> {
        			int level = s.length();
        			Block blk = s.get(level-1).setColour(levelColour(level));
        			return dir ? blk.translate(0, disp, VERTICAL_BLOCK_HEIGHT)
        					   : blk.translate(disp, 0, VERTICAL_BLOCK_HEIGHT);
        		}
        ));
        scene = stack.lift(block, sys.time, tLevel,
        		(blks, blk, t, tLev) -> {
        			float tt = (float)(t - tLev) * VERTICAL_SCROLL_VELOCITY;
        			if (tt > VERTICAL_BLOCK_HEIGHT) tt = VERTICAL_BLOCK_HEIGHT;
        		    return new Scene(blks.append(blk), blks.length() * VERTICAL_BLOCK_HEIGHT + tt , 1);
        		});
        sGameOver = Stream.filterOptional(sUpdate.map(su ->
            su.isPresent() ? Optional.empty()
            		       : Optional.of(Unit.UNIT)));
	}

	public final Cell<Scene> scene;
	public final Stream<Unit> sGameOver;
}