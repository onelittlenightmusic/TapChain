package org.tapchain.core;

public class PointValue extends Value<IPoint> {
	IPoint f;
	public PointValue(IPoint iPoint, IPoint f) {
		super(iPoint);
		this.f = f;
		_set(iPoint);
	}

	@Override
	public IPoint _get() {
		if(val instanceof WorldPoint)
			return ((WorldPoint)val).scalerNew(f);
		return val;
	}
}
