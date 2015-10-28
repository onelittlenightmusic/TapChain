package org.tapchain;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;

import org.tapchain.core.IPoint;
import org.tapchain.core.IPoint.WPEffect;
import org.tapchain.game.CarEngineer.Angle;
import org.tapchain.game.CarEngineer.RotationAcceleration;
import org.tapchain.game.CarEngineer.Speed;
import org.tapchain.game.MyFloat;
import org.tapchain.realworld.R;

import java.util.Calendar;

public class ShowInstance {
	static Paint circlePaintFill = new Paint();
	static Paint focuspaint = new Paint();
	static Paint smallText = new Paint();
	static float dh = 0.6f;
	static float dm = 1.0f;
	static NinePatchDrawable npd_num = null;
	static NinePatchDrawable npd_word = null;
	static NinePatchDrawable npd_inside = null;
	static {
		int textSize = 50;
		circlePaintFill.setAntiAlias(true);
		circlePaintFill.setColor(0xccffffff);
		circlePaintFill.setStyle(Paint.Style.FILL);
		focuspaint.setColor(0x40ffffff);
		focuspaint.setAntiAlias(true);
		focuspaint.setTextAlign(Paint.Align.CENTER);
		focuspaint.setTextSize(textSize);
		focuspaint.setStrokeWidth(9f);
		smallText.setTextSize(20f);
		smallText.setAntiAlias(true);
		smallText.setColor(0xffffffff);
		smallText.setTextAlign(Paint.Align.CENTER);
		smallText.setTypeface(Typeface.DEFAULT_BOLD);
	}
	static boolean showInstance(Canvas canvas, Object val, IPoint cp, Paint textPaint, Paint innerPaint/*, Paint innerPaint2*/) {
//		if(npd_num == null)
//			npd_num = (NinePatchDrawable) act.getResources().getDrawable(R.drawable.bubble5);
//		if(npd_word == null)
//			npd_word  = (NinePatchDrawable) act.getResources().getDrawable(R.drawable.paper);
//		if(npd_inside == null)
//			npd_inside  = (NinePatchDrawable) act.getResources().getDrawable(R.drawable.shadow2);
		if (val instanceof Integer) {
			float r = 25f;
			showInteger(canvas, r, (Integer) val, cp.x(), cp.y());
		} else if (val instanceof Float) {
			DrawLib.drawStringCenter(canvas, cp, String.format("%.2f",((Float)val)), textPaint);
		} else if (val instanceof Speed) {
			DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("Wheel", R.drawable.wheel, 100, 100), ((Speed)val).getPos(), cp, innerPaint);
			DrawLib.drawStringCenter(canvas, cp, String.format("%.1f mph",((Speed)val).get()), smallText);
		} else if (val instanceof Angle) {
			DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("Pedal", R.drawable.pedal), ((Angle)val).get()*5f, cp, innerPaint);
		} else if (val instanceof RotationAcceleration) {
			DrawLib.drawBitmapCenter(canvas, BitmapMaker.makeOrReuse("RotationAccel", R.drawable.rotation2), cp, innerPaint);
		} else if (val instanceof MyFloat) {
			DrawLib.drawStringCenter(canvas, cp, String.format("%.2f",((MyFloat)val).get()), textPaint);
		} else if (val instanceof IPoint) {
			IPoint valp = (IPoint) val;
			showPoint(canvas, valp, cp, innerPaint);
		} else if (val instanceof String) {
			DrawLib.drawStringCenter(canvas, cp, val.toString(), textPaint);
		} else if (val instanceof Calendar) {
			showCalendar(canvas, (Calendar)val, cp, 60f);
		}
		return true;
	}

	public static void showPoint(Canvas canvas, IPoint value, IPoint center, Paint innerPaint) {
		IPoint screenp;
		if (value.getEffect() == WPEffect.POS) {
			screenp = value;
			canvas.drawLine(center.x(), center.y(), screenp.x(),
					screenp.y(), innerPaint);
		} else {
			float centerx = center.x(), centery = center.y();
			float screenx = value.x(), screeny = value.y();
			canvas.drawLine(centerx, centery, centerx + 20f
					* screenx, center.y() + 20f * screeny,
					innerPaint);
			canvas.drawLine(
					centerx + 16f * screenx + 3f * screeny,
					centery + 16f * screeny - 3f * screenx,
					centerx + 20f * screenx, centery + 20f
							* screeny, innerPaint);
			canvas.drawLine(
					centerx + 16f * screenx - 3f * screeny,
					centery + 16f * screeny + 3f * screenx,
					centerx + 20f * screenx, center.y() + 20f
							* screeny, innerPaint);
		}

	}

	public void showInteger2(Canvas canvas, float r, int val, float x, float y) {
		circlePaintFill.setStrokeWidth(10);
		for (int i = 1; i <= val % 10; i++)
			canvas.drawCircle(x
					+ (float) (r * Math.sin(Math.PI * 0.2f * i)), y
					+ (float) (r * Math.cos(Math.PI * 0.2f * i)) - r,
					r * 0.4f, circlePaintFill);
		if (val >= 10) {
			circlePaintFill
					.setStrokeWidth(circlePaintFill.getStrokeWidth() * 2);
			showInteger(canvas, r * 2.5f, val / 10, x, y - r);
		}

	}

	static float[] dicex = new float[] { -1f, 1f, -1f, 1f, -1f, 1f, 0f, 0f};
	static float[] dicey = new float[] { -1f, 1f, 1f, -1f, 0f, 0f, -1f, 1f};
	public static void showInteger(Canvas canvas, float r, int val, float x, float y) {
		circlePaintFill.setStrokeWidth(10);
		int offset = val % 10;
		int even = offset - offset%2;
		int odd = offset%2;
		for (int i = 0; i < even; i++)
			canvas.drawCircle(x	+ r * dicex[i],
					y + r * dicey[i],
					r * 0.45f, circlePaintFill);
		if(odd == 1)
			canvas.drawCircle(x, y, r*0.5f, circlePaintFill);
		if (val >= 10) {
			circlePaintFill
					.setStrokeWidth(circlePaintFill.getStrokeWidth() * 2);
			showInteger(canvas, r, val / 10, x -
					3*r, y);
		}

	}

	public static void showCalendar(Canvas canvas, Calendar obj, IPoint _point, float l) {
		float diam = l * dm, diah = l * dh;
		canvas.drawCircle(_point.x(), _point.y(), diam, focuspaint);
			int _min = ((Calendar) obj).get(Calendar.MINUTE);
			int _hour = ((Calendar) obj).get(Calendar.HOUR);
			float min = (float)_min;
			float hour = (float)_hour+min/60f;
			Log.w("test", String.format("TimeTapStyle was shown.%d:%d",(int)hour, (int)min));
			canvas.drawLine(_point.x(), _point.y(), _point.x()+diah*(float)Math.sin(Math.PI/6f*hour), _point.y()-diah*(float)Math.cos(Math.PI/6f*hour), focuspaint);
			canvas.drawLine(_point.x(), _point.y(), _point.x()+diam*(float)Math.sin(Math.PI/30f*min), _point.y()-diam*(float)Math.cos(Math.PI/30f*min), focuspaint);
			DrawLib.drawStringCenter(canvas, _point, String.format("%02d:%02d", _hour, _min), focuspaint);

	}
}
