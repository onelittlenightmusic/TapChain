package org.tapchain.core;

import java.util.List;

public class TapMath {
	public static ScreenPoint getCurvePoint(float alpha, List<ScreenPoint> sp) {
		ScreenPoint rtn = new ScreenPoint();
		int i = 0;
		for(ScreenPoint pt: sp) {
//			float c;
			rtn = rtn.plus(pt.multiply(/*c = */bezier_coeff(alpha,i++)));
//			Log.w("TapChain", String.format("%f ^ %d = %f", alpha, i-1, c));
		}
		return rtn;
	}
	public static float bezier_coeff(float alpha, int i) {
		return (float) (Math.pow(alpha, i)*Math.pow(1f-alpha, 3-i)*((i%3==0)?1f:3f));
	}
	public static float pow(float alpha, int i) {
		if(i == 1)
			return alpha;
		else if(i <= 0)
			return 1;
		return alpha*pow(alpha, i-1);
	}

}
