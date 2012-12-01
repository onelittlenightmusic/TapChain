package org.tapchain.core;

public interface IPoint {
	public static enum WPEffect { POS, DIF }
	public int x();
	public int y();
	public IPoint sub(IPoint pt);
	public WPEffect getEffect();
	public IPoint setDif();
	public IPoint plus(IPoint pos);
	public IPoint multiply(float bezier_coeff);
	public IPoint round(int i);
	public IPoint add(int i, int j);
	public IPoint set(IPoint p);
	public IPoint add(IPoint effectValue);
}
