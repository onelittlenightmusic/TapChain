package org.tapchain.viewlib;

import org.tapchain.core.IPoint;

public interface IShapeBoundary {
	public boolean contains(IPoint ipoint, IPoint... fs);
}
