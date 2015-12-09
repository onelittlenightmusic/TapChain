package org.tapchain.game;

import org.tapchain.core.IPoint;

public interface ISensorView {
	IPoint getTilt();
	void shake(int interval);
}
