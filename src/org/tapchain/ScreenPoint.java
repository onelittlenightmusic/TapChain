package org.tapchain;

import org.tapchain.TapChainEditor.IWindow;

import android.graphics.Rect;


public class ScreenPoint implements HeroicPoint {
	public int x, y;

	public ScreenPoint() {
		super();
		x = 0;
		y = 0;
	}

	public ScreenPoint(int _x, int _y) {
		x = _x;
		y = _y;
	}

	public ScreenPoint(ScreenPoint pt) {
		x = pt.x;
		y = pt.y;
	}

	public ScreenPoint(float rawX, float rawY) {
		x = (int)rawX;
		y = (int)rawY;
	}

	public ScreenPoint add() {
		y += 100;
		return this;
	}
	
	public ScreenPoint clone() {
		return new ScreenPoint(this);
	}

	
	public WorldPoint getWorldPoint(IWindow v) {
		return new WorldPoint(this.x + IWindow.window_orient.x, this.y + IWindow.window_orient.y);
	}

	@Override
	public ScreenPoint sub(HeroicPoint sizeClosed) {
		return new ScreenPoint(this.x - sizeClosed.x(), this.y - sizeClosed.y());
	}

	public ScreenPoint plus(ScreenPoint screenPoint) {
		return new ScreenPoint(this.x + screenPoint.x, this.y + screenPoint.y);
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	public boolean isContained(Rect rect) {
		return rect.contains(this.x, this.y);
	}
}