package org.tapchain.core;



public class ScreenPoint implements IPoint {
	public float x, y;

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
	
	@Override
	public ScreenPoint subNew(IPoint sizeClosed) {
		return new ScreenPoint(this.x - sizeClosed.x(), this.y - sizeClosed.y());
	}

	public ScreenPoint plusNew(IPoint offset1) {
		return new ScreenPoint(this.x + offset1.x(), this.y + offset1.y());
	}
	
	@Override
	public float x() {
		return x;
	}

	@Override
	public float y() {
		return y;
	}

	public ScreenPoint multiplyNew(float coeff) {
		return new ScreenPoint(coeff*this.x, coeff*this.y);
	}

	@Override
	public WPEffect getEffect() {
		return null;
	}

	@Override
	public IPoint setDif() {
		return this;
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
	public IPoint plus(float d, float dy) {
		x += d;
		y += dy;
		return this;
	}

	@Override
	public IPoint set(IPoint p) {
		this.x = p.x();
		this.y = p.y();
		return this;
	}
	@Override
	public IPoint copy() {
		return new ScreenPoint(this);
	}
	@Override
	public boolean equals(IPoint p) {
		return x == p.x() && y == p.y();
	}

    @Override
    public IPoint setOffset(IValue<IPoint> pt) {
        return setOffset(pt, false);
    }

    @Override
	public IPoint setOffset(IValue<IPoint> pt, boolean keep) {
		return null;
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
	public float getAbs() {
		return (float)Math.sqrt(x*x+y*y);
	}

	@Override
	public IPoint unsetDif() {
		return this;
	}
	@Override
	public float theta() {
	    float angle = (float) Math.toDegrees(Math.atan2(x, y));

	    if(angle < 0){
	        angle += 360;
	    }
	    return angle;
	}

	@Override
	public IPoint multiply(float a) {
		return this;
	}

	@Override
	public IPoint unsetOffset(IValue<IPoint> pt, boolean keep) {
		return null;
	}

	@Override
	public IPoint plus(IPoint point) {
		return null;
	}

	@Override
	public IPoint ein() {
		return this.multiplyNew(1f/getAbs());
	}

	@Override
	public ScreenPoint clear() {
		return this;
	}
}