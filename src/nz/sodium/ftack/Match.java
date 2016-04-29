package nz.sodium.ftack;

import java.util.Optional;

import javaslang.collection.Array;
import nz.sodium.*;
import nz.sodium.time.*;


public class Match {
	public static final Scene emptyScene = new Scene(Array.of(
		new Block(new Point(-5, -5, 0),
					  new Point(5, 5, 1),
					  Match.levelColour(0))
		), 0, 1);
	public static Colour levelColour(int level) {
		return Colour.fromHSV((float)level * 6,0.4f,1.0f);
	}
	public Match(TimerSystem<Double> sys, Stream<Unit> sClick)
	{
		Cell<Boolean> direction = sClick.accum(false, (u, d) -> !d);
		Array<Block> stack0 = Array.of(
				new Block(new Point(-5, -5, 0),
						  new Point(5, 5, 1),
						  levelColour(0))
			);
	    CellLoop<Block> block = new CellLoop<>();
		double t0 = sys.time.sample();
        Cell<Double> tLevel = sClick.snapshot(sys.time).hold(t0);
		Cell<Float> displacement = sys.time.lift(tLevel,
				(t, tLev) -> 10 - (float)(t - tLev) * 12);
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
        stack.loop(Stream.filterOptional(sUpdate).hold(stack0));
        block.loop(displacement.lift(stack, direction,
        		(disp, s, dir) -> {
        			int level = s.length();
        			Block blk = s.get(level-1).setColour(levelColour(level));
        			return dir ? blk.translate(0, disp, 1)
        					   : blk.translate(disp, 0, 1);
        		}
        ));
        scene = stack.lift(block, sys.time, tLevel,
        		(blks, blk, t, tLev) -> {
        			float tt = (float)(t - tLev) * 3;
        			if (tt > 1) tt = 1;
        		    return new Scene(blks.append(blk), (float)blks.length() + tt, 1);
        		});
        sGameOver = Stream.filterOptional(sUpdate.map(su ->
            su.isPresent() ? Optional.empty()
            		       : Optional.of(Unit.UNIT)));
	}

	public final Cell<Scene> scene;
	public final Stream<Unit> sGameOver;
}