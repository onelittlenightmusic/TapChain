package org.tapchain;

public interface IPoint {
	public int x();
	public int y();
	public IPoint sub(IPoint pt);
}
