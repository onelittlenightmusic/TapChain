package org.tapchain.editor;

import org.tapchain.PaletteSort;
import org.tapchain.core.IPoint;
import org.tapchain.game.ISensorView;

public interface IWindow {
	void move(float f, float g);
	void onDraw();
	void log(String...strings);
	void showPalette(PaletteSort sort);
    boolean isInWindow(float x, float y);
    IPoint getMiddlePoint();
}