package org.tapchain.editor;

import org.tapchain.PaletteSort;
import org.tapchain.core.IPoint;

public interface IWindow {
	public IPoint getWindowSize();
	public void move(float f, float g);
	void onDraw();
	void log(String...strings);
	public void showPalette(PaletteSort sort);
}