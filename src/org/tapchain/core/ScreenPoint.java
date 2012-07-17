package org.tapchain.core;

import org.tapchain.core.TapChainEdit.IWindow;


public class ScreenPoint implements IPoint {
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
		return new WorldPoint(this.x , this.y );
	}

	@Override
	public ScreenPoint sub(IPoint sizeClosed) {
		return new ScreenPoint(this.x - sizeClosed.x(), this.y - sizeClosed.y());
	}

	public ScreenPoint plus(IPoint offset1) {
		return new ScreenPoint(this.x + offset1.x(), this.y + offset1.y());
	}
	
	public ScreenPoint plus(float x, float y) {
		return new ScreenPoint(this.x + x, this.y + y);
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	public ScreenPoint multiply(float coeff) {
		return new ScreenPoint(coeff*this.x, coeff*this.y);
	}
}