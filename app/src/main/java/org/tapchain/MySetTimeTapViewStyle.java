package org.tapchain;

import java.util.Calendar;

import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollHandler;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTapView;
import org.tapchain.viewlib.DrawLib;
import org.tapchain.viewlib.ShowInstance;

import android.graphics.Bitmap;
import android.graphics.Canvas;
public class MySetTimeTapViewStyle extends OptionTapViewStyle implements IScrollHandler, IRelease {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		float radi = 100f;
		Bitmap bm_fg = null;

		MySetTimeTapViewStyle(IActorTapView _p, Bitmap bm_fg) {
			super(_p);
			setSize(new WorldPoint(300f, 300f));
			this.bm_fg = bm_fg;
			this.registerHandler(this);
		}

		@Override
		public void view_init() {
			getPaint().setColor(0x77ffffff);
			_get().setOffset(getParentTap());
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
				int alpha) {
			DrawLib.drawBitmapCenter(canvas, bm_fg, cp, getPaint());
			IPoint _point = getParentTap()._get();
			Object obj = getParentTap().getMyActorValue();
			if(obj instanceof Calendar) {
				ShowInstance.showCalendar(canvas, (Calendar)obj, _point, 150f);
			}
			return true;
		}
		
		public void setParentValue(IPoint pos, IPoint vp) {
			float theta = 90+pos.subNew(getParentTap().getCenter()).theta();
			Calendar cal = Calendar.getInstance();
			int hour = (int) (theta/30f), min = (int)((theta/30f%1f)*12f)*5;
			cal.set(Calendar.HOUR, hour);
			cal.set(Calendar.MINUTE, min);
			getParentTap().setMyActorValue(cal);
		}

		@Override
		public void onScroll(ITapChain edit, IActorTapView tap, IPoint pos, IPoint vp) {
			setParentValue(pos, vp);
		}

		@Override
		public boolean onRelease(ITapChain edit, IPoint pos) {
			getParentTap().commitMyActorValue();
            return true;
		}
}
