package org.tapchain.game;

import org.tapchain.core.ChainException;
import org.tapchain.core.Consumer;
import org.tapchain.core.Effector;
import org.tapchain.core.Filter;
import org.tapchain.core.Generator;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.core.PathType;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.ViewActor;

public class CarEngineer {
	public static class Engine extends Effector<Tire, AccelAngle> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8605258204785337945L;

		public Engine() {
			super();
//			setPull(true);
		}
		@Override
		public void effect(IValue<AccelAngle> _e, Tire input) throws ChainException {
			input._get().set((_e._get().get()+input._get().get()));
			input._get().increment();
		}
	}
	
	public static class Brake extends Filter<RotationAcceleration, BrakeAngle, RotationAcceleration> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8605258204785337945L;
		
		public Brake() {
			super();
			_set(new RotationAcceleration());
			setControlled(true);
//			setPull(true);
		}
		@Override
		public RotationAcceleration func(IValue<RotationAcceleration> val, BrakeAngle input) {
			Float angleSpeed = -input.get();
			val._get().set(angleSpeed < 0f ? angleSpeed : 0f);
//			input.increment();
			return val._get();
		}

		@Override
		public void init(IValue<RotationAcceleration> val) {

		}
	}
	
	public static class Engine2 extends Filter<RotationAcceleration, AccelAngle, RotationAcceleration> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8605258204785337945L;

		public Engine2() {
			super();
			_set(new RotationAcceleration());
			setControlled(true);
//			setPull(true);
		}
		@Override
		public RotationAcceleration func(IValue<RotationAcceleration> val, AccelAngle input) {
			Float angleSpeed = input.get();
			val._get().set(angleSpeed > 0f ? angleSpeed : 0f);
//			input.increment();
			return val._get();
		}

		@Override
		public void init(IValue<RotationAcceleration> val) {

		}
	}
	
	public static class Tire extends Consumer<Speed, RotationAcceleration> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3198338409546562125L;

		Float tireRadius = 0.5f;
		Float maxSpeed = 200f;
		public Tire() {
			super();
			super._set(new Speed());
		}
		@Override
		public void consume(IValue<Speed> val, RotationAcceleration input) {
			float nextSpeed = val._get().get() + tireRadius*input.get();
			if(nextSpeed < 0f) nextSpeed = 0f;
			else if(nextSpeed > maxSpeed) nextSpeed = maxSpeed;
			val._get().set(nextSpeed);
			val._get().increment();
		}

		@Override
		public void init(IValue<Speed> val) {

		}

		@Override
		public boolean _set(Speed value) {
			try {
				((ViewActor)getParent(PathType.FAMILY))._set(value);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			return super._set(value);
		}
		
	}
	
	public static class RepeatRoad extends Effector.ValueArrayEffector<IPoint> implements Road {
		private static final long serialVersionUID = -3203024211611501934L;
		float radius = 300f;
		IPoint lastPoint = null, nextGoal = null;
		float lastPos = 0f;

		public RepeatRoad() {
			super();
			setAutoStart();
			setAutoEnd();
		}

		@Override
		public IPoint getPosXY(Float pos) {
			if(lastPoint == null) {
				lastPos = pos;
				return lastPoint = _get();
			}
			IPoint newPoint = (nextGoal == null) ? _get() : nextGoal;
			for(float restd = pos - lastPos; ;) {
				IPoint vec = newPoint.subNew(lastPoint);
				float dist = vec.getAbs();
				if(dist > restd) {
					lastPoint = vec.ein().multiply(restd).plus(lastPoint);
					nextGoal = newPoint;
					break;
				} else {
					lastPoint = newPoint;
					newPoint = _get();
					restd -= dist;
				}
			}
			lastPos = pos;
			return lastPoint;
				
		}
		
		@Override
		public void effect(IValue<IPoint> _e, IValue<IPoint> _t) throws ChainException {
			Speed s = (Speed)_t._get();
			L("1.202 ValueEffector valueSet").go(_t._set(s.set(getPosXY(s.getPos()))));
		}
	}
	
	public static class AccelPedal extends Generator.SimpleGenerator<AccelAngle> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5369704401264903553L;

		public AccelPedal() {
			super();
		}
		
		public AccelPedal(Float f, Boolean hippo) {
			super(new CarEngineer.AccelAngle(f), hippo);
		}

		@Override
		public void init(IValue<AccelAngle> val) {
			val._set(new AccelAngle());
		}
	}
	
	public static class BrakePedal extends Generator.SimpleGenerator<BrakeAngle> {
		public BrakePedal() {
			super();
		}
		public BrakePedal(Float f, Boolean hippo) {
			super(new CarEngineer.BrakeAngle(f), hippo);
		}

		@Override
		public void init(IValue<BrakeAngle> val) {
			val._set(new BrakeAngle());
		}
	}
	
	public static class Angle extends MyFloat {
		public Angle() {
			super();
		}
		public Angle(Float f) {
			super(f);
		}
		@Override
		public String toString() {
			return String.format("%.1f",f);
		}
	}
	
	public static class BrakeAngle extends Angle {
		public BrakeAngle() {
			super();
		}
		public BrakeAngle(Float f) {
			super(f);
		}
	}
	
	public static class AccelAngle extends Angle {
		public AccelAngle() {
			super();
		}
		public AccelAngle(Float f) {
			super(f);
		}
	}
	
	public static class RotationAcceleration extends MyFloat {
		public RotationAcceleration() {
			super();
		}
		public RotationAcceleration(Float f) {
			super(f);
		}
	}

	public static class Speed extends WorldPoint implements IFloat {
		Float pos = 0f;
		Float speed = 0f;
		public Speed() {
			super();
		}
		public Speed(Float f) {
			super();
		}
		public Float getPos() {
			return pos;
		}
		
		public void increment() {
			pos += get();
		}
		@Override
		public void set(Float f) {
			speed = f;
		}
		@Override
		public Float get() {
			return speed;
		}
	}
	
	public interface Road {
		public IPoint getPosXY(Float pos);
	}
}