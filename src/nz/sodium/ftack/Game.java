package nz.sodium.ftack;

import java.util.Optional;
import nz.sodium.*;
import nz.sodium.time.*;


public class Game {
	private static float interpolate(float x0, float x1, float frac) {
	    return x0 + (x1 - x0) * frac;
	}
    public Game(TimerSystem<Double> sys, Stream<Unit> sClick) {
    	StreamLoop<Unit> sGameOver = new StreamLoop<>();
    	CellLoop<Optional<Match>> playing = new CellLoop<>();
    	Stream<Unit> sStart = sClick.gate(playing.map(p -> !p.isPresent()));
    	Stream<Match> sNewMatch = sStart.map(u -> new Match(sys, sClick));
    	playing.loop(sNewMatch.map(m -> Optional.of(m))
    			.orElse(sGameOver.map(m -> Optional.empty()))
    			.hold(Optional.empty()));
    	sGameOver.loop(Cell.switchS(playing.map(om ->
    	    om.isPresent() ? om.get().sGameOver
    	    		       : new Stream<Unit>())));
    	double t0 = sys.time.sample();
    	Cell<Double> tTransition = sStart.orElse(sGameOver)
    			                         .snapshot(sys.time).hold(t0);
    	Cell<Scene> finalScene = sGameOver.<Optional<Match>, Scene>snapshot(playing,
				(u, om) -> om.isPresent() ? om.get().scene.sample()
						                  : Match.emptyScene)
    		.hold(Match.emptyScene);
    	scene = Cell.switchC(
			sys.time.<Optional<Match>, Scene, Double, Cell<Scene>>lift(playing, finalScene, tTransition,
				(t, om, fs, tT) -> {
					float zoomFrac0 = (float)(t - tT) * 3;
					if (zoomFrac0 > 1) zoomFrac0 = 1;
					final float zoomFrac = zoomFrac0;
					float zoomOut0 = 15 / (float)fs.blocks.length();
					if (zoomOut0 > 0.4f) zoomOut0 = 0.4f;
					final float zoomOut = zoomOut0;
					final float zoomCentre = (float)fs.blocks.length() * 0.5f;
					if (om.isPresent())   // If playing
						return om.get().scene.map(sc ->
						    sc.zoom(interpolate(zoomCentre, sc.zCentre, zoomFrac),
						    		interpolate(zoomOut, 1, zoomFrac)));
					else
						return new Cell<Scene>(
					        fs.zoom(interpolate(fs.zCentre, zoomCentre, zoomFrac),
					        		interpolate(1, zoomOut, zoomFrac)));
				}
			)
		);
    }
	public final Cell<Scene> scene;
}
