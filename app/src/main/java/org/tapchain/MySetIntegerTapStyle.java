package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollHandler;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class MySetIntegerTapStyle extends OptionTapStyle implements IScrollHandler, IRelease {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected float radi = 100f;
	protected Paint standardpaint = new Paint();
	protected Paint focuspaint = new Paint();
	int textSize = 20;
	protected float startangle = -90f, oneangle = 36f, spaceangle = 3f;

	MySetIntegerTapStyle(IActorTap _p) {
		super(_p);
		setSize(new WorldPoint(200f, 200f));
		this.registerHandler(this);
	}

	@Override
	public void view_init() {
		getPaint().setColor(0x77ffffff);
		_get().setOffset(getParentTap());
		standardpaint.setColor(0x40ffffff);
		standardpaint.setAntiAlias(true);
		standardpaint.setTextAlign(Paint.Align.CENTER);
		standardpaint.setTextSize(textSize);
		focuspaint.setColor(0x40aaaaff);
		focuspaint.setAntiAlias(true);
		focuspaint.setTextAlign(Paint.Align.CENTER);
		focuspaint.setTextSize(textSize);
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size,
			int alpha) {
		IPoint _point = getParentTap()._get();
		float d = 150f;
		RectF r = new RectF(_point.x()-d, _point.y()-d,_point.x()+d, _point.y()+d);
		for (int i = 0; i < 10; i++) {
			standardpaint.setColor(0x22ffffff + 0x11000000*i);
			if(equalMyValue(i))
				canvas.drawArc(r,
						(float)i*oneangle+startangle+spaceangle, oneangle-spaceangle, true, focuspaint);
			else
				canvas.drawArc(r,
						(float)i*oneangle+startangle+spaceangle, oneangle-spaceangle, true, standardpaint);
			float theta = (float)Math.PI*((oneangle*((float)i+0.5f)+startangle)/180f);
				canvas.drawText(Integer.toString(i), _point.x() + radi * (float)Math.cos(theta), 
						_point.y() + radi * (float)Math.sin(theta), standardpaint);
		}
		standardpaint.setColor(0x40ffffff);
		return true;
	}
	
	public boolean equalMyValue(Integer val) {
		return getParentTap().getMyActorValue() == val;
	}
	
	public void setParentValue(IPoint pos, IPoint vp) {
		float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/36f;
		if (j < 0) j += 10f;
		getParentTap().setMyActorValue((int)j);
	}
	
	public void commitParentValue() {
		getParentTap().commitMyActorValue();
	}

	@Override
	public void onScroll(IEditor edit, IActorTap tap, IPoint pos, IPoint vp) {
		setParentValue(pos, vp);
	}

	@Override
	public boolean onRelease(IEditor edit, IPoint pos) {
        if(((WorldPoint)pos.subNew(getParentTap().getCenter())).len() < 150f) {
            setParentValue(pos, null);
            commitParentValue();
            return true;
        }
        return false;
	}
}