package org.tapchain.game;

import org.tapchain.MySetFloatTapViewStyle;
import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTapView;

public class MySetPedalTapViewStyle extends MySetFloatTapViewStyle {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7318531384117387864L;

	public MySetPedalTapViewStyle(IActorTapView _p) {
			super(_p);
			startangle = -30f;
			oneangle = 10f;
		}

	@Override
	public void setParentValue(IPoint pos, IPoint vp) {
		float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
		if(j < 0) j = 0f;
		else if(j > 10) j = 10;
		j = Math.round(j*10f)*0.1f;
		((MyFloat)getParentTap().getMyActorValue()).set(j);
	}

	@Override
	public boolean equalMyValue(Integer val) {
		return val == ((MyFloat)getParentTap().getMyActorValue()).get().intValue();
	}

}
