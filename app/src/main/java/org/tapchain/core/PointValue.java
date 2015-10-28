package org.tapchain.core;

public class PointValue extends Value<IPoint> {
	IPoint f;
	public PointValue(IPoint iPoint, IPoint f) {
		super(iPoint);
		this.f = f;
		_valueSet(iPoint);
	}

	@Override
	public IPoint _valueGet() {
		if(val instanceof WorldPoint)
			return ((WorldPoint)val).scalerNew(f);
		return val;
	}
}
