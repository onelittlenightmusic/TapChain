package org.tapchain;

import org.tapchain.core.D2Point;
import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IScrollHandler;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MySetPathTapStyle extends OptionTapStyle implements IScrollHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	float radi = 10f;
	Paint focuspaint = new Paint();
	Bitmap bm_fg = null;
//	IPoint offset = new WorldPoint(100, 100);

	MySetPathTapStyle(IActorTap _p, Bitmap bm_fg) {
		super(_p);
		setSize(new WorldPoint(200f, 200f));
		this.bm_fg = bm_fg;
		registerHandler(this);
	}

	@Override
	public void view_init() {
		getPaint().setColor(0x77ffffff);
		_valueGet().setOffset(getParentTap());
		focuspaint.setColor(0x40ffffff);
		focuspaint.setAntiAlias(true);
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
		DrawLib.drawBitmapCenter(canvas, bm_fg, cp, getPaint());
		IPoint _point = getParentTap()._valueGet();
		canvas.drawCircle(_point.x(), _point.y(), 300, focuspaint);
		return true;
	}

	@Override
	public void onScroll(IEditor edit, IActorTap tap, IPoint pos, IPoint vp) {
		getParentTap().setMyActorValue(new D2Point(pos/*(WorldPoint)pos.subNew(getParentTap().getCenter())*/, vp));
	}
}