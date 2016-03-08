package org.tapchain;

import java.io.Serializable;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.editor.ColorLib;
import org.tapchain.editor.ColorLib.ColorCode;
import org.tapchain.core.LinkBooleanSet;
import org.tapchain.core.LinkType;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.viewlib.DrawLib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

public class BalloonTapStyle extends OptionTapStyle implements Serializable, IBlueprintFocusNotification, IRelease {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ShapeDrawable dOut;
	public Bitmap bm_fg = null, bm_face = null;
	Paint paint = new Paint();
	String str = null;
	String tag = null;
	public enum Direction {
		TOP(0,-1),BOTTOM(0,1),LEFT(-1,0),RIGHT(1,0);
		private float x, y;
		Direction(float x, float y) {
			this.x = x;
			this.y = y;
		}
		public float[] getRectA(RectF r) {
			float x1, y1, x3, y3;
			switch(this) {
			case TOP:
				x1 = r.left + 20;
				y1 = r.bottom;
				x3 = x1 + 30f;
				y3 = y1;
				break;
			case BOTTOM:
				x1 = r.left + 20;
				y1 = r.top;
				x3 = x1 + 30f;
				y3 = y1;
				break;
			case LEFT:
				x1 = r.right;
				y1 = r.top +20f;
				x3 = x1;
				y3 = y1 + 30f;
				break;
			default:
				x1 = r.left;
				y1 = r.top +20f;
				x3 = x1;
				y3 = y1 + 30f;
				}
			return new float[] {x1, y1, x3, y3};
		}
		public static Direction getDirection(float x, float y) {
			if(x > y) {
				if(x > -y) {
					return RIGHT;
				} else {
					return TOP;
				}
			}
			if(x > -y)
				return BOTTOM;
			return LEFT;
		}
	}
	private Direction dir = Direction.RIGHT;
	private LinkType ac = LinkType.PUSH;
	public BalloonTapStyle(IActorTap t) {
		super(t);
		setMyActor(t.getActor());
		setSize(new WorldPoint(60f, 60f));
		float r = 15;
		dOut = new ShapeDrawable(new RoundRectShape(new float[] { r, r, r,
				r, r, r, r, r }, null, null));
		dOut.getPaint().setAntiAlias(true);
		dOut.getPaint().setColor(0xaaaaaaaa);
		dOut.getPaint().setStyle(Paint.Style.STROKE);
//		dOut.getPaint().setPathEffect(
//				new DashPathEffect(new float[] { 10f, 5f }, 0));
		dOut.getPaint().setStrokeWidth(4);
		paint.setColor(Color.argb(255, 255, 255, 255));
		paint.setTextSize(50);
		paint.setTextAlign(Align.CENTER);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
	}
	
	public BalloonTapStyle(IActorTap t, LinkType al, ClassEnvelope c, String tag) {
		this(t);
//		ce = c;
		Bitmap i = BitmapMaker.getClassImage(c, 50, 50);
		setMyActorValue(i);
		this.tag = tag;
	}
	
	@Override
	public ViewActor setCenter(IPoint p) {
		ViewActor rtn = super.setCenter(p);
		dir = Direction.getDirection(getCenter().rawX(), getCenter().rawY());
		return rtn;
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
//		switch(getConnect()) {
//		case PUSH:
//			DrawLib.drawBitmapCenter(canvas, bm_plug_male, pt_plug_male, getPaint());
//			break;
//		case PULL:
//			DrawLib.drawBitmapCenter(canvas, bm_plug_female, pt_plug_female, getPaint());
//		}
//		IPoint pt = (getConnect()==ActorConnect.PULL) ? pt_plug_female : pt_plug_male;
//		ShowPlug.showPlug(canvas, getClassEnvelope(), cp, 0, getConnect() == ActorConnect.PULL, getPaint());
		int halfx = (int)size.x()/2, halfy = (int)size.y()/2;
		canvas.save();
		canvas.translate(cp.x(), cp.y());
		dOut.setBounds(-halfx, -halfy, halfx, halfy);
		dOut.draw(canvas);
		if(bm_fg != null)
			DrawLib.drawBitmapCenter(canvas, bm_fg, WorldPoint.zero(), getPaint());
//		if(str != null)
//			DrawLib.drawStringCenter(canvas, cp, str, paint);
		canvas.restore();
		RectF r = getScreenRectF();
		float[] d = dir.getRectA(r);
		RectF r2 = ((ActorTap)getParentTap()).getScreenRectF();
		float x2 = r2.left + 20f, y2 = r2.top + 25f;
		Path p = new Path();
		p.moveTo(d[0], d[1]);
		p.lineTo(x2, y2);
		p.lineTo(d[2], d[3]);
		canvas.drawPath(p, dOut.getPaint());
		return true;
	}
	
	public boolean setMyActorValue(Object obj) {
		if(obj instanceof Bitmap) {
			bm_fg = (Bitmap) obj;
		} else if(obj instanceof String) {
			str = (String) obj;
		} else if(obj instanceof Integer) {
			str = obj.toString();
		} else if(obj instanceof Float) {
			str = String.format("%.2f",((Float)obj));
		}
		return true;
	}
	
	public String getTag() {
		return tag;
	}
	
	@Override
	public AndroidView setColorCode(ColorCode colorCode) {
		ColorMatrixColorFilter matrix = AndroidColorCode.getColorMatrix(colorCode);
		dOut.getPaint().setColorFilter(matrix);
		paint.setColorFilter(matrix);
		return super.setColorCode(colorCode);
	}


	public void onFocus(LinkBooleanSet booleanSet) {
		if(booleanSet != null && booleanSet.hasAnyConnect()) {
			for(LinkType ac: booleanSet) {
					setColorCode(ColorLib.getLinkColor(ac));
			}
			return;
		}
		setColorCode(ColorCode.CLEAR);
	}

	@Override
	public boolean onRelease(IEditor edit, IPoint pos) {
        return true;
	}

    static String _out = "_out", _in = "_in";
    public static OptionTapStyle createBalloon(IActorTap t, LinkType linkType, ClassEnvelope ce) {
        OptionTapStyle balloon = null;
        balloon = new BalloonTapStyle(t, linkType, ce, linkType.getOutOrIn() ? _out : _in);
        balloon.setCenter(new WorldPoint(100f, 100f));
        balloon._get().setOffset(t);
        return balloon;
    }
}