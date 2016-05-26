package org.tapchain;

import org.tapchain.editor.TapManager;
import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTapView;

import android.graphics.Bitmap;

public class MyExitOptionTapViewStyle extends MySimpleOptionTapViewStyle {
	public MyExitOptionTapViewStyle(IActorTapView t, Bitmap bm_fg) {
		super(t, bm_fg);
	}

    @Override
	public boolean onRelease(ITapChain tapChain, IPoint pos) {
        new TapManager(tapChain).remove(getParentTap().getActor());
        return true;
	}
}
