package org.tapchain;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.ColorLib.ColorCode;
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

	BeamTapStyle(Resources r, IActorTap _p, Bitmap bm_fg) {
		super(_p);
		setSize(new WorldPoint(200f, 200f));
		bitmap = new BitmapDrawable(r, bm_fg);
		sizey = Math.max(bitmap.getIntrinsicHeight(), bitmap.getBitmap().getHeight());
		sizex = Math.max(bitmap.getIntrinsicWidth(), bitmap.getBitmap().getWidth());
		bitmap.setTileModeX(Shader.TileMode.REPEAT);
		IPoint initPos = new WorldPoint(200f, 0f);
		setCenter(initPos);
		calcTheta(initPos.plusNew(_p.getCenter()));
//		bitmap.setTargetDensity(DisplayMetrics.DENSITY_MEDIUM);
		// registerHandler(this);
	}

	@Override
	public void view_init() {
		getPaint().setColor(0x77ffffff);
		_valueGet().setOffset(getParentTap(), false);
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
//		sizex = sizex - sizex % 60;
//		sizex = sizex * canvas.getDensity() / 160;
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

//	@Override
//	public void onScroll(IEdit edit, IActorTap act, IPoint pos, IPoint vp) {
//		super.onScroll(edit, act, pos, vp);
//		calcTheta(pos);
//	}
	
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

	// public void onAttach(IPoint pos, IEdit edit) {
	// ActorLink ac = getConnect();
	// IActorTap parent = getParentTap();
	// getEventHandler().setLastPushed(ac, parent);
	// ClassEnvelope ce = getClassEnvelope();
	// edit.highlightConnectables(ac.reverse(), ce);
	// // edit.log("onSelect", String.format("Balloon on %s[%s]",
	// parent.getMyActor().getName(), ac.toString()));
	//
	// }

	@Override
	public boolean onInside(IActorEditor edit, IActorTap t2, Actor a1, Actor a2) {
//		Log.w("test", "Beam's onInside called");
		boolean rtn = false;
		if (edit.connect(a1, LinkType.PUSH, a2)) {
//			if (null != edit.edit().append(a2, PathType.OFFER, a1,
//					PathType.OFFER, true)) {
			rtn = true;
		}
		return rtn;

	}

}