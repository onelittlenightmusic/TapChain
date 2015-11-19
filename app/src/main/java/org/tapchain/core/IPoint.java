package org.tapchain.core;

import java.io.Serializable;


public interface IPoint {
	public static enum WPEffect implements Serializable { POS, DIF }
	float x();
	float y();
	IPoint copy();
	IPoint subNew(IPoint pt);
	WPEffect getEffect();
	IPoint setDif();
	IPoint unsetDif();
	IPoint plusNew(IPoint pos);
	IPoint multiplyNew(float bezier_coeff);
	IPoint round(int i);
	IPoint plus(float i, float j);
	IPoint set(IPoint p);
	boolean equals(IPoint p);
	String toString();
    IPoint setOffset(IValue<IPoint> pt);
	IPoint setOffset(IValue<IPoint> pt, boolean keep);
	IPoint unsetOffset(IValue<IPoint> pt, boolean keep);
	float rawX();
	float rawY();
	float getAbs();
	float theta();
	IPoint multiply(float a);
	IPoint plus(IPoint iPoint);
	IPoint ein();
	IPoint clear();
	
}
