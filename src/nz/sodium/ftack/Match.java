package nz.sodium.ftack;

import java.util.Optional;

import javaslang.collection.Array;
import nz.sodium.*;
import nz.sodium.time.*;


public class Match {
	private static Colour levelColour(int level) {
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
				(t, tLev) -> 10 - (float)(t - tLev) * 10);
        Cell<Array<Block>> stack =
        		sClick.accum(stack0, (Unit u, Array<Block> s) -> {
        			boolean dir = direction.sample();
        			int level = s.length();
        			Block below = s.get(level-1);
        			Block blk = block.sample();
        			float disp = displacement.sample();
        			Optional<Tuple2<Block,Block>> oBlk;
        			if (dir)
        				if (disp >= 0)
        					oBlk = blk.chopY(below.p1.y);
        				else
        					oBlk = blk.chopY(below.p0.y);
        			else
        				if (disp >= 0)
        					oBlk = blk.chopX(below.p1.x);
        				else
        					oBlk = blk.chopX(below.p0.x);
        			if (oBlk.isPresent())
	        		    return s.append(disp >= 0 ? oBlk.get().a
	        		    		                  : oBlk.get().b);
        			else
        				return s;
        		});
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
	}

	public final Cell<Scene> scene;
}