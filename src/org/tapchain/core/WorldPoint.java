package org.tapchain.core;

import org.tapchain.core.TapChainEdit.IWindow;


public class WorldPoint implements IPoint {
	public int x, y;
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
	public WorldPoint plus(IPoint pos) {
		if(pos==null) return this;
		return new WorldPoint(x+pos.x(), y+pos.y());
	}
	public WorldPoint add(int d) {
		add(d, d);
		return this;
	}
	@Override
	public WorldPoint add(int dx, int dy) {
		x += dx;
		y += dy;
		return this;
	}
	@Override
	public WorldPoint add(IPoint wp) {
		x += wp.x();
		y += wp.y();
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
	@Override
	public String toString() {
		return String.format("%d/%d", x, y);
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
	
	@Override
	public WorldPoint round(int denominator) {
		if(x < 0)
			x -= denominator;
		x -= x % denominator;
		if(y < 0)
			y -= denominator;
		y -= y % denominator;
		return this;
	}
	public boolean equals(WorldPoint p) {
		return x == p.x && y == p.y;
	}
	@Override
	public IPoint set(IPoint p) {
		this.x = p.x();
		this.y = p.y();
		return this;
	}
}
