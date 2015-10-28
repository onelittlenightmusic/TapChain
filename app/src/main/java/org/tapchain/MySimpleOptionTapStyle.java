package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;

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
		_valueGet().setOffset(getParentTap(), false);
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
		DrawLib.drawBitmapCenter(canvas, bm_fg, cp, getPaint());
		return true;
	}
	
//	public String getAttribute() {
//		return attr;
//	}
	@Override
	public void onRelease(IPoint pos, IEditor edit) {
//		if(getAttribute() == "exit")
//			edit.edit().remove(((Controllable)getParentTap().getMyActor()));
//		else if(getAttribute() == "restart")
//			((Controllable)getParentTap().getMyActor()).interrupt(ControllableSignal.RESTART);

	}
	
	
}