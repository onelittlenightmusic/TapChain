package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTap;

import android.graphics.Bitmap;

public class MyExitOptionTapStyle extends MySimpleOptionTapStyle {
	public MyExitOptionTapStyle(IActorTap t, Bitmap bm_fg) {
		super(t, bm_fg);
	}

	public void onRelease(IEditor edit, IPoint pos) {
		edit.edit().remove(getParentTap().getActor());
	}
}
