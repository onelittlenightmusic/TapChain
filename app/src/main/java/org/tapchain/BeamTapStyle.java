package org.tapchain;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.editor.ColorLib.ColorCode;
import org.tapchain.core.Actor;
import org.tapchain.core.LinkType;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.IActorEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IAttachHandler;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

public class BeamTapStyle extends AdapterTapStyle implements IAttachHandler<IActorEditor, Actor, IActorTap> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	float theta = 10f, length = 100f;
	Paint focuspaint = new Paint();
	BitmapDrawable bitmap = null;
	int sizex = 0, sizey = 0;

	// IPoint offset = new WorldPoint(100, 100);

	BeamTapStyle(Resources r, IActorTap _p, Bitmap bm_fg, IPoint initPos) {
		super(_p);
		setSize(new WorldPoint(100f, 100f));
		bitmap = new BitmapDrawable(r, bm_fg);
		sizey = Math.max(bitmap.getIntrinsicHeight(), bitmap.getBitmap().getHeight());
		sizex = Math.max(bitmap.getIntrinsicWidth(), bitmap.getBitmap().getWidth());
		bitmap.setTileModeX(Shader.TileMode.REPEAT);
		if(initPos == null)
			initPos = new WorldPoint(200f, 0f);
		init(initPos);
	}

	BeamTapStyle(Resources r, IActorTap _p, Bitmap bm_fg) {
		this(r, _p, bm_fg, null);
	}

	public void init(IPoint initPos) {
		setCenter(initPos);
		calcTheta(initPos.plusNew(getParentTap().getCenter()));
	}

	@Override
	public void view_init() {
		getPaint().setColor(0x77ffffff);
		_get().setOffset(getParentTap());
		focuspaint.setColor(0x40ffffff);
		focuspaint.setAntiAlias(true);
	}

	@Override
	public AndroidView setColorCode(ColorCode colorCode) {
		ColorMatrixColorFilter matrix = AndroidColorCode
				.getColorMatrix(colorCode);
		bitmap.getPaint().setColorFilter(matrix);
		return super.setColorCode(colorCode);
	}

	@Override
	public boolean view_user(Canvas canvas, IPoint cp, IPoint size, int alpha) {
		IPoint parent = getParentTap().getCenter();
		canvas.save();
		canvas.translate(parent.x(), parent.y());
		canvas.rotate(theta);
		canvas.translate(sizey / 2, -sizey / 2);
		if (bitmap != null) {
			int lenint = (int) length;
			lenint = lenint / sizex;
			bitmap.setBounds(0, 0, sizex * lenint, sizey);
			bitmap.draw(canvas);
		}
		canvas.restore();
		return true;
	}

	@Override
	public ViewActor setCenter(IPoint pos) {
		super.setCenter(pos);
		calcTheta(pos);
		return this;
	}
	
	private void calcTheta(IPoint pos) {
		IPoint v = pos.subNew(getParentTap().getCenter());
		theta = v.theta();
		length = v.getAbs();
	}


	@Override
	public boolean onTouch(IActorEditor edit, IActorTap t2, Actor a1, Actor a2) {
		boolean rtn = false;
		if (edit.connect(a1, LinkType.PUSH, a2)) {
			rtn = true;
		}
		return rtn;

	}

}