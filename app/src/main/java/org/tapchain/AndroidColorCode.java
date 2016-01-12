package org.tapchain;

import org.tapchain.editor.ColorLib.ColorCode;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class AndroidColorCode {
	static ColorMatrixColorFilter colorTransformRed, colorTransformGreen, colorTransformBlue, colorTransform, colorTransformYellow;
	static {
		colorTransformRed = createMatrix(new float[]{ 1f, 0f, 0f, 0f, 0, 0f, 0.2f, 0f, 0f, 0,
			0f, 0f, 0.2f, 0f, 0, 0f, 0f, 0f, 1f, 0 });
		colorTransformGreen = createMatrix(new float[]{ 0.2f, 0f, 0f, 0f, 0,
			0f, 1f, 0f, 0f, 0,
			0f, 0f, 0.2f, 0f, 0, 0f, 0f, 0f, 1f, 0 });
		colorTransformBlue = createMatrix(new float[]{ 0.2f, 0f, 0f, 0f, 0,
			0f, 0.8f, 0f, 0f, 0,
			0f, 0f, 1.6f, 0f, 0,
			0f, 0f, 0f, 1f, 0 });
		colorTransformYellow = createMatrix(new float[]{ 1f, 0f, 0f, 0f, 0, 0f, 1f, 0f, 0f, 0,
				0f, 0f, 0.2f, 0f, 0, 0f, 0f, 0f, 1f, 0 });
		colorTransform = createMatrix(new float[]{ 1f, 0f, 0f, 0f, 0, 0f, 1f, 0f, 0f, 0,
				0f, 0f, 1f, 0f, 0, 0f, 0f, 0f, 1f, 0 });
	}
	public static ColorMatrixColorFilter getColorMatrix(ColorCode c) {
		switch(c) {
		case CLEAR:
			return colorTransform;
		case RED:
			return colorTransformRed;
		case GREEN:
			return colorTransformGreen;
		case YELLOW:
			return colorTransformYellow;
		case BLUE:
			return colorTransformBlue;
		default:
			return colorTransform;
		}
	}
	static ColorMatrixColorFilter createMatrix(float[] color) {
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0f); // Remove Colour
		colorMatrix.set(color); // Apply the Red

		ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(
				colorMatrix);
		return colorFilter;
	}

}
