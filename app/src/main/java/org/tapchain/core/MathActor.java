package org.tapchain.core;

import org.tapchain.core.actors.ViewActor;

@SuppressWarnings("serial")
public class MathActor {
	public static class MathEffector<EFFECT> extends Effector.ValueEffector<EFFECT> {
		public MathEffector(Class<? extends EFFECT> cls) {
			super();
			once();
		}
		@Override
		public void effect(IValue<EFFECT> _e, IValue<EFFECT> _t) throws ChainException {
			L("mathEffect").go(_t._set(mathEffect(_t._get(), _e._get())));
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
	public abstract static class IntegerVerb<V> extends Effector<V, Integer> implements IStep {
		public IntegerVerb() {
			super();
			setDuration(1);
//			setPull(true);
		}
		@Override
		public void effect(IValue<Integer> _e, V _t) throws ChainException {
			verb(_t,_e._get());
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
