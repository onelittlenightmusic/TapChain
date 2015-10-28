package org.tapchain.game;

import org.tapchain.core.IPoint;

public interface ISensorView {
	public IPoint getTilt();
	public void shake(int interval);
}
