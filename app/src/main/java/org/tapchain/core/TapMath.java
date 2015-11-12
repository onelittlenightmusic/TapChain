package org.tapchain.core;

import org.tapchain.core.Actor.Filter;

import java.util.List;

@SuppressWarnings("serial")
public class TapMath {
	public static class Round extends Filter<Object, Object, Object> {
		@Override
		public Object func(IValue<Object> val, Object obj) {
			return null;
		}

		@Override
		public void init(IValue<Object> val) {

		}
	}
	
	public static class Sin extends Filter<Object, Object, Object> {
		@Override
		public Object func(IValue<Object> val, Object obj) {
			return null;
		}

		@Override
		public void init(IValue<Object> val) {

		}
	}
	
	public static IPoint getCurvePoint(float alpha, List<IPoint> list) {
		IPoint rtn = new WorldPoint();
		int i = 0;
		for(IPoint pt: list) {
            float f = bezier_coeff(alpha,i++);
			rtn.plus(f*pt.x(), f*pt.y());
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
