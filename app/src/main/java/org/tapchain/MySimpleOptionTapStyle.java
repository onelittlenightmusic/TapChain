package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.viewlib.DrawLib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MySimpleOptionTapStyle extends OptionTapStyle implements IRelease {
	Paint focuspaint = new Paint();
	Bitmap bm_fg = null;
//	String attr = null;

	public MySimpleOptionTapStyle(IActorTap t, Bitmap bm_fg) {
		super(t);
		setSize(new WorldPoint(100f, 100f));
		this.bm_fg = bm_fg;
//		this.attr = name;
	}
	@Override
	public void view_init() {
		_get().setOffset(getParentTap());
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
		DrawLib.drawBitmapCenter(canvas, bm_fg, cp, getPaint());
		return true;
	}
	
	@Override
	public boolean onRelease(IEditor edit, IPoint pos) {
        return true;
	}
}
