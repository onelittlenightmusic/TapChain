package org.tapchain.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class WorldPoint implements IPoint, Comparable, Serializable {
	public float x, y;
	private WPEffect effect = WPEffect.POS;
	private HashMap<IValue<IPoint>, Float> offset = null;

	public WorldPoint() {
		super();
	}

	public WorldPoint(int _x, int _y) {
		x = _x;
		y = _y;
	}
	
	public WorldPoint(IPoint p) {
		x = p.x();
		y = p.y();
	}
	
	public WorldPoint(WorldPoint pt) {
		x = pt.x;
		y = pt.y;
		effect = pt.effect;
		if(pt.offset != null) {
			offset = new HashMap<>();
			offset.putAll(pt.offset);
		}
	}

	public WorldPoint(float x2, float y2) {
		x = x2;
		y = y2;
	}
	
	@Override
	public WorldPoint clear() {
		x = 0f;
		y = 0f;
		offset = null;
		return this;
	}

	@Override
	public WorldPoint plusNew(IPoint pos) {
		if(pos==null) return this;
		return new WorldPoint(this).plus(pos.x(), pos.y()).setDif(getEffect());
	}
	public WorldPoint plus(float d) {
		plus(d, d);
		return this;
	}
	@Override
	public WorldPoint plus(float dx, float dy) {
		x += dx;
		y += dy;
		return this;
	}
	@Override
	public IPoint plus(IPoint point) {
		return plus(point.x(), point.y());
	}
	public WorldPoint divide(float n) {
		return new WorldPoint(x()/n, y()/n);
	}
	@Override
	public WorldPoint multiplyNew(float a) {
		return new WorldPoint(this).multiply(a);
	}
	
	public WorldPoint multiplyNew(float ax, float ay) {
		return new WorldPoint(this).multiply(ax, ay);
	}
	@Override
	public WorldPoint multiply(float a) {
		multiply(a, a);
		return this;
	}

	public WorldPoint multiply(float ax, float ay) {
		set(ax*x(), ay*y());
		return this;
	}
	public WorldPoint scalerNew(IPoint b) {
		return multiplyNew(b.x(), b.y());
	}
	@Override
	public WorldPoint subNew(IPoint b) {
		if(b==null) return new WorldPoint(this);
		return new WorldPoint(this).sub(b);
	}
	
	public WorldPoint sub(IPoint b) {
		setRawDif(-b.x(), -b.y());
		return this;
	}
	@Override
	public String toString() {
		return String.format("%f/%f", x(), y());
	}
	
	private float cumulateX() {
		float rtn = 0;
		if(offset != null) {
			for (Map.Entry<IValue<IPoint>, Float> pt: offset.entrySet())
				rtn += pt.getKey()._valueGet().x() * pt.getValue();
		}
		return rtn;
	}

	private float cumulateY() {
		float rtn = 0;
		if(offset != null) {
			for (Map.Entry<IValue<IPoint>,Float> pt: offset.entrySet())
				rtn += pt.getKey()._valueGet().y() * pt.getValue();
		}
		return rtn;
	}

	@Override
	public float x() {
		return x+cumulateX();
	}

	@Override
	public float y() {
		return y+cumulateY();
	}

	@Override
	public WorldPoint setDif() {
		this.effect = WPEffect.DIF;
//		Log.w("test", String.format("%s setDif", toString()));
		return this;
	}

	@Override
	public WorldPoint unsetDif() {
		this.effect = WPEffect.POS;
		return this;
	}

	public WPEffect getEffect() {
		return effect;
	}
	
	private WorldPoint setDif(WPEffect effect) {
		if(effect == WPEffect.DIF)
			return this.setDif();
		return this.unsetDif();
	}
	
	public static WorldPoint minus(WorldPoint wp) {
		if(wp==null) return new WorldPoint();
		return new WorldPoint(-wp.x(), -wp.y());
	}
	
	public WorldPoint minus(float i, float j) {
		return subNew(new WorldPoint(i, j));
	}
	
	public float getAbs() {
		float _x = x(), _y = y();
		return (float) Math.sqrt(_x * _x + _y * _y);
	}
	
	@Override
	public WorldPoint round(int denominator) {
		if(x() < 0)
			x -= denominator;
		x -= x() % denominator;
		if(y() < 0)
			y -= denominator;
		y -= y() % denominator;
		return this;
	}
	@Override
	public boolean equals(IPoint p) {
		return x() == p.x() && y() == p.y();
	}
	@Override
	public IPoint set(IPoint p) {
		switch(p.getEffect()) {
		case POS:
			set(p.x(), p.y());
			break;
		case DIF:
			setRawDif(p.x(), p.y());
			break;
		}
		return this;
	}
	
	public IPoint set(float px, float py) {
			setRaw(px-cumulateX(),py-cumulateY());
		return this;
	}
	@Override
	public IPoint copy() {
		return new WorldPoint(this);
	}

    @Override
	public IPoint setOffset(IValue<IPoint> pt) {
		return setOffset(pt, 1f);
	}

	public IPoint setOffset(IValue<IPoint> pt, float alpha) {
		if(offset == null)
			offset = new HashMap<IValue<IPoint>, Float>();
		if(offset.containsKey(pt))
			return this;
		if(pt._valueGet() == this)
			return this;
		offset.put(pt, alpha);
		return this;
	}
	
	@Override
	public IPoint setOffset(IValue<IPoint> pt, boolean keep) {
		float oldx = x(), oldy = y();
		setOffset(pt);
		if(keep)
			set(oldx, oldy);
//		Log.w("test", String.format("setOffset %s", pt._valueGet().getDetails(), keep));
		return this;
	}

	@Override
	public float rawX() {
		return x;
	}

	@Override
	public float rawY() {
		return y;
	}
	
	@Override
	public float theta() {
	    float angle = (float) Math.toDegrees(Math.atan2(y(), x()));

//	    if(angle < 0){
//	        angle += 360;
//	    }
	    return angle;
	}

	@Override
	public IPoint unsetOffset(IValue<IPoint> pt, boolean keep) {
		float oldx = x(), oldy = y();
		if(offset == null)
			return this;
		if(offset.containsKey(pt)) {
			offset.remove(pt);
			if(keep)
				set(oldx, oldy);
		}
		return this;
	}

	public IPoint setRaw(float px, float py) {
		this.x = px;
		this.y = py;
		return this;
	}
	
	public IPoint setRawDif(float dx, float dy) {
		this.x += dx;
		this.y += dy;
		return this;
	}

	@Override
	public IPoint ein() {
		return this.multiplyNew(1f/getAbs());
	}

	@Override
	public int compareTo(Object arg0) {
		int rtn = -1;
		if(arg0 instanceof WorldPoint) {
			WorldPoint p = (WorldPoint)arg0;
//			if(x() > p.x())
//				rtn = 1;
//			else if(x() == p.x()) {
//				if(y() > p.y()) {
//					rtn = 1;
//				} else if(y() == p.y()) {
//					rtn = 0;
//				} else {
//					rtn = -1;
//				}
//			} else {
//				rtn = -1;
//			}
			int thisz = Zorder(this, 100, 100);
			int pz = Zorder(p, 100, 100);
			if(thisz > pz) return 1;
			else if(thisz == pz) return 0;
			else return -1;
		}
		return rtn;
	}
	
	
    static int[] MASKS = {0x55555555, 0x33333333, 0x0F0F0F0F, 0x00FF00FF};
    static int[] SHIFTS = {1, 2, 4, 8};
	public static int Zorder(WorldPoint p, int xunit, int yunit) {
	    int x = (int) (p.x()/xunit)+1024;
	    int y = (int) (p.y()/yunit)+1024;
	    x = (x | (x << SHIFTS[3])) & MASKS[3];
	    x = (x | (x << SHIFTS[2])) & MASKS[2];
	    x = (x | (x << SHIFTS[1])) & MASKS[1];
	    x = (x | (x << SHIFTS[0])) & MASKS[0];
	    y = (y | (y << SHIFTS[3])) & MASKS[3];
	    y = (y | (y << SHIFTS[2])) & MASKS[2];
	    y = (y | (y << SHIFTS[1])) & MASKS[1];
	    y = (y | (y << SHIFTS[0])) & MASKS[0];
		return x | (y << 1);
	}

	static final WorldPoint zero = new WorldPoint(0f, 0f);
	public static WorldPoint zero() {
		return zero;
	}

	public float len() {
		float x = x(), y = y();
		return (float)Math.sqrt((double)(x*x+y*y));
	}

}
