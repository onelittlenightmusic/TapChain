package org.tapchain.core;

import org.tapchain.core.TapChainEdit.IWindow;


public class WorldPoint implements IPoint {
	public int x, y;
	public static enum WPEffect { POS, DIF }
	private WPEffect effect = WPEffect.POS;

	public WorldPoint() {
		super();
	}

	public WorldPoint(int _x, int _y) {
		x = _x;
		y = _y;
	}
	
	public WorldPoint(WorldPoint pt) {
		x = pt.x;
		y = pt.y;
	}

	public WorldPoint(float x2, float y2) {
		x = (int)x2;
		y = (int)y2;
	}

	public WorldPoint clone() {
		return new WorldPoint(this);
	}
	public WorldPoint plus(WorldPoint b) {
		if(b==null) return this;
		return new WorldPoint(x+b.x, y+b.y);
	}
	public WorldPoint plus(int d) {
		x += d;
		y += d;
		return this;
	}
	public WorldPoint divide(int n) {
		return new WorldPoint(x/n, y/n);
	}
	public WorldPoint multiply(float a) {
		return new WorldPoint((int)(a*x), (int)(a*y));
	}
	public WorldPoint sub(IPoint b) {
		if(b==null) return this;
		return new WorldPoint(x-b.x(), y-b.y());
	}
	public ScreenPoint getScreenPoint(IWindow v) {
		if(v == null) 
			return new ScreenPoint(x, y);
		return new ScreenPoint(this.x, this.y);
	}
	@Override
	public String toString() {
		return ""+x+"/"+y;
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	public WorldPoint setDif() {
		this.effect = WPEffect.DIF;
		return this;
	}

	public WPEffect getEffect() {
		return effect;
	}
	
	public static WorldPoint minus(WorldPoint wp) {
		if(wp==null) return new WorldPoint();
		return new WorldPoint(-wp.x(), -wp.y());
	}
	
	public int getAbs() {
		return (int)Math.sqrt(x * x + y * y);
	}
	
	public WorldPoint round(int denominator) {
		x -= x % denominator;
		y -= y % denominator;
		return this;
	}
	public boolean equals(WorldPoint p) {
		return x == p.x && y == p.y;
	}
}
