package org.tapchain;

import java.io.Serializable;

import org.tapchain.TapChainEditor.IWindow;

import android.graphics.Rect;


@SuppressWarnings("serial")
public class WorldPoint implements Serializable, HeroicPoint {
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
	public WorldPoint divide(int n) {
		return new WorldPoint(x/n, y/n);
	}
	public WorldPoint multiply(float a) {
		return new WorldPoint((int)(a*x), (int)(a*y));
	}
	public WorldPoint sub(HeroicPoint b) {
		if(b==null) return this;
		return new WorldPoint(x-b.x(), y-b.y());
	}
	public ScreenPoint getScreenPoint(IWindow v) {
		if(v == null) 
			return new ScreenPoint(x, y);
		return new ScreenPoint(this.x - IWindow.window_orient.x, this.y - IWindow.window_orient.y);
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
	
	public boolean isContained(Rect r) {
		return r.contains(this.x, this.y);
	}
	
}
