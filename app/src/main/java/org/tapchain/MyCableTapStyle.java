package org.tapchain;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.ColorLib.ColorCode;
import org.tapchain.core.Actor;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.LinkBooleanSet;
import org.tapchain.core.LinkType;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorAttachHandler;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;

import java.io.Serializable;

public class MyCableTapStyle extends AdapterTapStyle implements Serializable, IBlueprintFocusNotification, IRelease, IActorAttachHandler, IFocusable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Bitmap bm_fg = null, bm_face = null;
	IPoint pt_plug_female = new WorldPoint(-100, 0);
	String str = null;
	String tag = null;
	float xpush = 0.8f, ypush = 0.2f;
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
	public MyCableTapStyle(IActorTap t) {
		super(t);
		setMyActor(t.getActor());
		setSize(new WorldPoint(80f, 80f));
		float r = 20;
		Paint paint = getPaint();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(5f);
		paint.setColor(Color.argb(255, 255, 255, 255));
		paint.setTextSize(50);
		paint.setTextAlign(Align.CENTER);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
//		bm_plug_male = BitmapMaker.makeOrReuse("plug_male", R.drawable.plug_male);
//		bm_plug_female = BitmapMaker.makeOrReuse("plug_female", R.drawable.plug_female);
//		pt_plug_male.setOffset(t, false);
//		pt_plug_female.setOffset(t, false);
//		_valueGet().setOffset(t, true);
	}
	
//	public MyBalloonTapStyle(ITap t, Bitmap fg, String tag) {
//		this(t);
//		//Set my actor to parent tap's actor for heap connection
//		setMyActorValue(fg);
//		this.tag = tag;
//	}
//	
	public MyCableTapStyle(IActorTap t, LinkType al, ClassEnvelope c, String tag) {
		this(t);
		ce = c;
		setLink(al);
//		Bitmap i = BitmapMaker.getClassImage(c);
//		setMyActorValue(i);
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
		RectF r2 = ((ActorTap)getParentTap()).getScreenRectF();
		float x0, y0, x1, y1, x2, y2, theta;
		if(getLink() == LinkType.PUSH) {
			x0 = cp.x();
			y0 = cp.y();
			x2 = r2.right;
			y2 = r2.centerY();
			x1 = x2 + Math.abs(x0 - x2) * xpush + Math.abs(y0 - y2) * ypush;
			y1 = y2;
			theta = (float)Math.toDegrees(Math.atan2(y0 - y1, x0 - x1));
		} else {
			x0 = cp.x();
			y0 = cp.y();
			x2 = r2.left;
			y2 = r2.centerY();
			x1 = x2 - Math.abs(x0 - x2) * xpush - Math.abs(y0 - y2) * ypush;
			y1 = y2;
			theta = (float)Math.toDegrees(Math.atan2(y0 - y1, x0 - x1))+180f;
		}
		Path p = new Path();
		p.moveTo(x0, y0);
		p.quadTo(x1, y1, x2, y2);
		canvas.drawPath(p, getPaint());
		canvas.save();
		canvas.translate(x0, y0);
		ShowPlug.showPlug(canvas, getClassEnvelope(), WorldPoint.zero(), theta, getLink() == LinkType.PULL, getPaint());
		canvas.restore();
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
		getPaint().setColorFilter(matrix);
		getPaint().setColorFilter(matrix);
		return super.setColorCode(colorCode);
	}

	@Override
	public void onFocus(LinkBooleanSet booleanSet) {
//		if(f != highlight) {
//			if(highlight) {
//				switch(ac) {
//				case PULL:
//					setColorFilter(ColorLib.colorTransformYellow);
//					break;
//				case PUSH:
//					setColorFilter(ColorLib.colorTransformBlue);
//					break;
//				case FROM_PARENT:
//					setColorFilter(ColorLib.colorTransformGreen);
//					break;
//				}
//			}
//			else {
//				setColorFilter(ColorLib.colorTransform);
////				Log.w("test", String.format("%s unlighted", getTag()));
//			}
//		}
		if(booleanSet != null && booleanSet.hasAnyConnect()) {
			for(LinkType ac: LinkType.values()) {
				if(booleanSet.isTrue(ac)) {
					setColorCode(ColorLib.getLinkColor(ac));
					return;
				}
			}
		}
		setColorCode(ColorCode.CLEAR);

	}

	@Override
	public void onRelease(IPoint pos, IEditor edit) {
		super.onRelease(pos,edit);
		MyCableTapStyle balloon = this;
		LinkType ac = balloon.getLink();
		IActorTap parent = balloon.getParentTap();
//		if(getSharedHandler() != null)
//			getSharedHandler().setLastPushed(ac, parent);
		ClassEnvelope ce = balloon.getClassEnvelope();
		edit.highlightConnectables(ac.reverse(), ce);
//		edit.log("onSelect", String.format("Balloon on %s[%s]", parent.getMyActor().getName(), ac.toString()));

	}

	String heapOut = "_out", heapIn = "_in";
	@Override
	public boolean onInside(IActorEditor edit, IActorTap t2, Actor a1, Actor a2) {
		boolean rtn = false;
			if(getTag().equals(heapOut)) {
				if(edit.connect(a1, LinkType.PUSH, a2)) {
//					if(null != edit.edit().append(a2, PathType.OFFER, a1, PathType.OFFER, true)) {
//					Log.w("test", String.format("%s heap connection succeeded", ((MyBalloonTapStyle) t1).getParentTap().getTag()));
//					unsetPushOutBalloon(getParentTap());
					rtn = true;
//					break;
				} else {
//					Log.w("test", String.format("%s heap connection failed", ((MyBalloonTapStyle) t1).getParentTap().getTag()));
				}
//			} else if (null != edit.edit().append(a1, PathType.OFFER, a2,
//					PathType.OFFER, true)) {
			} else if (edit.connect(a1, LinkType.PULL, a2)) {
//				Log.w("test", String.format("%s heap connection succeeded", t2.getTag()));
//				if(t2 instanceof MyCableTapStyle)
//					unsetPushOutBalloon(((MyCableTapStyle)t2).getParentTap());
//				else
//					unsetPushOutBalloon(t2);
				rtn = true;
//				break;
			} else {
//				Log.w("test", String.format("%s heap connection failed", t2.getTag()));
			}
			return rtn;

	}

	@Override
	public void focus(LinkType al) {
	}

	@Override
	public void unfocus() {
	}
}