package org.tapchain;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IPoint;
import org.tapchain.game.CarEngineer.RotationAcceleration;
import org.tapchain.realworld.R;

public class ShowPlug {
	public static void showPlug(Canvas canvas, ClassEnvelope ce, IPoint pt, float theta, boolean female, Paint paint) {
		if(ce.getRawClass().equals(RotationAcceleration.class)) {
			if(female) {
				DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("hex_female", R.drawable.hex_female), theta, pt, paint);
			} else {
				DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("hex_male", R.drawable.hex_male), theta, pt, paint);
			}
		} else {
			if(female) {
				DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("plug_female",R.drawable.plug_female), theta, pt, paint);
			} else {
				DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("plug_male", R.drawable.plug_male), theta, pt, paint);
			}
		}
	}
}
