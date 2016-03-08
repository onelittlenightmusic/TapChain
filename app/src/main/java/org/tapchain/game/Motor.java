package org.tapchain.game;

import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.ChainException;
import org.tapchain.core.Effector;
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
	
	public static class MotorPedal extends Effector<Motor, Angle> {
		@Override
		public void effect(IValue<Angle> _e, Motor _t) throws ChainException {
			_t.setSpeed(_e._get());
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
