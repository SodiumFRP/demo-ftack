package nz.sodium.ftack;

import java.util.Optional;

import javaslang.collection.Array;
import nz.sodium.*;
import nz.sodium.time.*;


public class Match {

	public static final Block initialBlock =
		new Block(new Point(-5, -5, 0),
				  new Point(5, 5, 1),
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
        scene = time.map(t ->
	        		new Scene(
        				Array.of(
        						initialBlock.translate(
        								(float)(t - t0), 0, 0)
						),
        				0, 1
					)
	    		);
        sGameOver = sClick;
	}

	public final Cell<Scene> scene;
	public final Stream<Unit> sGameOver;
}