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

	@Override
	public WPEffect getEffect() {
		return null;
	}

	@Override
	public IPoint setDif() {
		return null;
	}

	@Override
	public IPoint round(int denominator) {
		if(x < 0)
			x -= denominator;
		x -= x % denominator;
		if(y < 0)
			y -= denominator;
		y -= y % denominator;
		return this;
	}

	@Override
	public IPoint add(int d, int dy) {
		x += d;
		y += dy;
		return this;
	}

	@Override
	public IPoint add(IPoint wp) {
		x += wp.x();
		y += wp.y();
		return this;
	}
	@Override
	public IPoint set(IPoint p) {
		this.x = p.x();
		this.y = p.y();
		return this;
	}
}