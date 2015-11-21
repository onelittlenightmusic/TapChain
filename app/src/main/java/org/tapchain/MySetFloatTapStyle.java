package org.tapchain;

import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTap;

public class MySetFloatTapStyle extends MySetIntegerTapStyle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected MySetFloatTapStyle(IActorTap _p) {
		super(_p);

	}
	@Override
	public void setParentValue(IPoint pos, IPoint vp) {
		float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
		if (j < 0) j += 10f;
		j = Math.round(j*10f)*0.1f;
		getParentTap().setMyActorValue(j);
	}

	@Override
	public boolean equalMyValue(Integer val) {
		return val == ((Float)getParentTap().getMyActorValue()).intValue();
	}
	@Override
	public void onRelease(IEditor edit, IPoint pos) {
		setParentValue(pos, null);
		getParentTap().commitMyActorValue();
	}
	
	
}
