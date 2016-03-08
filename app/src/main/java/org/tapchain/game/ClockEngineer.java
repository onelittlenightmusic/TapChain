package org.tapchain.game;

import android.graphics.Canvas;

import org.tapchain.AndroidActor.AndroidImageView;
import org.tapchain.viewlib.DrawLib;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.realworld.R;

public class ClockEngineer {
	public static class Pin extends AndroidImageView {
		float theta = 0f;
		float dt = 30f;
		int x = 0;
		public Pin(Float dt) {
			super();
            __addLinkClass(Pin.class, LinkType.PULL, Integer.class);
            __addLinkClass(Pin.class, LinkType.TO_CHILD, Pin.class);
			this.dt = dt;
		}
		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			DrawLib.drawBitmapCenter(canvas, bm_scaled, theta, sp, getPaint());
			return true;
		}
	}
	
	public static class LongPin extends Pin {
		/**
		 * 
		 */

		public LongPin() {
			super(6f);
			setImage(R.drawable.pinlong);
		}
	}
	public static class ShortPin extends Pin {
		/**
		 * 
		 */

		public ShortPin() {
			super(30f);
			setImage(R.drawable.pinshort);
		}

	}
}
