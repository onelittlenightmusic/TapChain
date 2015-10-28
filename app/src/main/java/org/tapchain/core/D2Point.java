package org.tapchain.core;

public class D2Point extends WorldPoint {
	IPoint vec = null;
	public D2Point() {
		super();
	}
	
	public D2Point(IPoint pos, IPoint vp) {
		super((WorldPoint)pos);
		setVector(vp);
	}
	
	public IPoint getVector() {
		return vec;
	}
	
	public D2Point setVector(IPoint p) {
		vec = p;
		return this;
	}
	
	public void evolve() {
		this.plus(vec);
	}
}