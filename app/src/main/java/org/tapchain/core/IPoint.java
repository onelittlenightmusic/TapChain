package org.tapchain.core;

import java.io.Serializable;


public interface IPoint {
	public static enum WPEffect implements Serializable { POS, DIF }
	public float x();
	public float y();
	public IPoint copy();
	public IPoint subNew(IPoint pt);
	public WPEffect getEffect();
	public IPoint setDif();
	public IPoint unsetDif();
	public IPoint plusNew(IPoint pos);
	public IPoint multiplyNew(float bezier_coeff);
	public IPoint round(int i);
	public IPoint plus(float i, float j);
	public IPoint minus(float i, float j);
	public IPoint set(IPoint p);
	boolean equals(IPoint p);
	public String toString();
	public IPoint setOffset(IValue<IPoint> pt, boolean keep);
	public IPoint unsetOffset(IValue<IPoint> pt, boolean keep);
	public float rawX();
	public float rawY();
	public float getAbs();
	public float theta();
	public IPoint multiply(float a);
	public String getDetails();
	public IPoint plus(IPoint iPoint);
	public IPoint ein();
	public IPoint clear();
	
}
