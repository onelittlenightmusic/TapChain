package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTap;

import android.graphics.Bitmap;

public class MyRestartOptionTapStyle extends MySimpleOptionTapStyle {
	public MyRestartOptionTapStyle(IActorTap t, Bitmap bm_fg) {
		super(t, bm_fg);
	}

	public void onRelease(IEditor edit, IPoint pos) {
		((Controllable)getParentTap().getActor()).interrupt(ControllableSignal.RESTART);
	}
}
