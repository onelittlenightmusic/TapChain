package org.tapchain.editor;

import org.tapchain.core.IPoint;

public interface IWindow {
	void move(float f, float g);
	void onDraw();
	void log(String...strings);
	void showPalette(PaletteSort sort);
    boolean isInWindow(float x, float y);
    IPoint getMiddlePoint();
}