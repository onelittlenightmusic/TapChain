package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

@SuppressWarnings("serial")
public class MathActor {
	public static class MathEffector<EFFECT> extends Actor.ValueEffector<EFFECT> {
		public MathEffector(Class<? extends EFFECT> cls) {
			super();
			once();
		}
		@Override
		public void effect(IValue<EFFECT> _t, EFFECT _e) throws ChainException {
			L("mathEffect").go(_t._valueSet(mathEffect(_t._valueGet(), _e)));
		}
		public EFFECT mathEffect(EFFECT x, EFFECT y) {
			return x;
		}
	}
	public static class IntegerMath extends MathEffector<Integer> {
		public IntegerMath() {
			super(Integer.class);
			initEffectValue(1, 1);
		}
	}
	public static class AddMath extends IntegerMath {
		public AddMath() {
			super();
		}
		@Override
		public Integer mathEffect(Integer x, Integer y) {
			Integer rtn = x;
				rtn += y;
			return rtn;
		}
	}
	
	public static class SubMath extends IntegerMath {
		public SubMath() {
			super();
		}
		@Override
		public Integer mathEffect(Integer x, Integer y) {
			Integer rtn = x;
				rtn -= y;
			return rtn;
		}
	}
	
	
	public static class MultiMath extends IntegerMath {
		public MultiMath() {
			super();
		}
		@Override
		public Integer mathEffect(Integer x, Integer y) {
			Integer rtn = x;
				rtn *= y;
			return rtn;
		}
	}
	public abstract static class IntegerVerb<V> extends Actor.OriginalEffector<V, Integer> implements IStep {
		public IntegerVerb() {
			super();
			setDuration(1);
			setPull(true);
		}
		@Override
		public void effect(V _t, Integer _e) throws ChainException {
			verb(_t,_e);
			if(_t instanceof ViewActor) {
				((ViewActor) _t).invalidate();
			}
		}
		
		public abstract void verb(V _t, Integer i);

		@Override
		public void onStep() {
			interruptStep();
		}
	}
	

}
