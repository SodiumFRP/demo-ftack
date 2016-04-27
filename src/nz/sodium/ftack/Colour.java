package nz.sodium.ftack;

public class Colour {
    public Colour(float r, float g, float b) {
    	this.r = r;
    	this.g = g;
    	this.b = b;
    }
    /**
     * Convert (hue, saturation, value) to RGB. Hue is in degrees.
     * @param h Hue
     * @param s Saturation
     * @param v Value
     * @return Colour
     */
    public static Colour fromHSV(float h, float s, float v) {
        int hi = (int)(h/60f) % 6;
    	float f = mod1(h/60f);
    	float p = v * (1 - s);
    	float q = v * (1 - f * s);
    	float t = v * (1 - (1 - f) * s);
    	switch (hi) {
    	case 0:  return new Colour(v, t, p);
    	case 1:  return new Colour(q, v, p);
    	case 2:  return new Colour(p, v, t);
    	case 3:  return new Colour(p, q, v);
    	case 4:  return new Colour(t, p, v);
    	default: return new Colour(v, p, q);
    	}
    }
    private static float mod1(float x) {
    	return x - (float)Math.floor(x);
    }
    public final float r;
    public final float g;
    public final float b;
}
