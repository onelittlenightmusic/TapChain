package org.tapchain.core;

public class PhysicalPoint extends D2Point {
	float mass = 1f;
	public PhysicalPoint() {
		super();
	}
	
	public PhysicalPoint(IPoint pos, IPoint vec, Float mass) {
		super(pos, vec);
		setMass(mass);
	}
	
	public void setMass(float mass) {
		this.mass = mass;
	}
	
	public void evolve(IPoint acc) {
		vec.plus(acc.multiply(1f/mass));
		evolve();
	}
}
