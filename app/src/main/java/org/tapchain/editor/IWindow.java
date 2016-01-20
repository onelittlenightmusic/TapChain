package org.tapchain.editor;

import org.tapchain.core.IPoint;

public interface IWindow {
	void onDraw();
	void log(String...strings);
	void showPalette(PaletteSort sort);
    IPoint getMiddlePoint();
    void run(Runnable runnable);
}