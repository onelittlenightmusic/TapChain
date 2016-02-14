package org.tapchain.game;

import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.IValue;
import org.tapchain.core.Self;
import org.tapchain.game.CarEngineer.Angle;

public class Motor extends Controllable<Self, Electricity, Void, Void> implements IValue<Float> {
	Float speed = 0f;
	public Motor() {
		super();
	}
	
	public void setSpeed(Angle a) {
		speed = a.get();
	}
	
	public static class MotorPedal extends OriginalEffector<Motor, Angle> {
		@Override
		public void effect(Motor _t, Angle _e) throws ChainException {
			_t.setSpeed(_e);
		}
		
	}

	@Override
	public boolean _set(Float value) {
		speed = value;
		return true;
	}

	@Override
	public Float _get() {
		return speed;
	}
}
