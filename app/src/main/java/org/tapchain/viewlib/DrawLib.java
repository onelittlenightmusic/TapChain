package org.tapchain.viewlib;

import org.tapchain.core.IPoint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawLib {

	public static void drawBitmapCenter(Canvas canvas, Bitmap bm, IPoint center,
			Paint paint) {
		drawBitmapCenter(canvas, bm, center.x(), center.y(), paint);
	}
	
	public static void drawBitmapCenter(Canvas canvas, Bitmap bm, float x, float y,
			Paint paint) {
		canvas.drawBitmap(bm, x - bm.getWidth() / 2,
				y - bm.getHeight() / 2, paint);
	}
	
	public static void drawBitmapCenter(Canvas canvas, Bitmap bm, Float th, IPoint center,
			Paint paint) {
		canvas.save();
		canvas.rotate(th%360f, center.x(), center.y());
		drawBitmapCenter(canvas, bm, center, paint);
		canvas.restore();
	}
	
	public static void drawStringCenter(Canvas canvas, IPoint p, String str, Paint paint) {
		canvas.drawText(str, p.x(), p.y()- ((paint.descent() + paint.ascent()) / 2), paint);
	}

}
