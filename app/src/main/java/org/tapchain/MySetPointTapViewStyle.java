package org.tapchain;

import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.core.IScrollHandler;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTapView;
import org.tapchain.viewlib.DrawLib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MySetPointTapViewStyle extends OptionTapViewStyle implements IScrollHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	float radi = 10f;
	Paint focuspaint = new Paint();
	Bitmap bm_fg = null;

	MySetPointTapViewStyle(IActorTapView _p, Bitmap bm_fg) {
		super(_p);
		setSize(new WorldPoint(200f, 200f));
		this.bm_fg = bm_fg;
		registerHandler(this);
	}

	@Override
	public void view_init() {
		getPaint().setColor(0x77ffffff);
			setCenter(getParentTap()._get());
		focuspaint.setColor(0x40ffffff);
		focuspaint.setAntiAlias(true);
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
		DrawLib.drawBitmapCenter(canvas, bm_fg, cp, getPaint());
		IPoint _point = getParentTap()._get();
		canvas.drawCircle(_point.x(), _point.y(), 300, focuspaint);
		return true;
	}

	@Override
	public void onScroll(ITapChain edit, IActorTapView tap, IPoint pos, IPoint vp) {
			setCenter(pos);
			getParentTap().setMyActorValue(vp/*.plusNew(offset)*/);
	}
}