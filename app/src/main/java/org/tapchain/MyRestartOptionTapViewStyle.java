package org.tapchain;

import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTapView;

import android.graphics.Bitmap;

public class MyRestartOptionTapViewStyle extends MySimpleOptionTapViewStyle {
	public MyRestartOptionTapViewStyle(IActorTapView t, Bitmap bm_fg) {
		super(t, bm_fg);
	}

    @Override
	public boolean onRelease(ITapChain edit, IPoint pos) {
		((Controllable)getParentTap().getActor()).interrupt(ControllableSignal.RESTART);
        return true;
	}
}
