package org.tapchain.game;

import org.tapchain.core.Actor.Filter;
import org.tapchain.core.Actor.OriginalEffector;
import org.tapchain.core.Actor.Generator;
import org.tapchain.core.Actor.ValueArrayEffector;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.PathType;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.core.WorldPoint;

public class CarEngineer {
	public static class Engine extends OriginalEffector<Tire, AccelAngle> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8605258204785337945L;

		public Engine() {
			super();
			setPull(true);
		}
		@Override
		public void effect(Tire input, AccelAngle _e) throws ChainException {
			input._valueGet().set((_e.get()+input._valueGet().get()));
			input._valueGet().increment();
		}
	}
	
	public static class Brake extends Filter<RotationAcceleration, BrakeAngle, RotationAcceleration> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8605258204785337945L;
		
		public Brake() {
			super();
			_valueSet(new RotationAcceleration());
			setControlled(true);
//			setPull(true);
		}
		@Override
		public RotationAcceleration func(IValue<RotationAcceleration> val, BrakeAngle input) {
			Float angleSpeed = -input.get();
			val._valueGet().set(angleSpeed < 0f ? angleSpeed : 0f);
//			input.increment();
			return val._valueGet();
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
			_valueSet(new RotationAcceleration());
			setControlled(true);
//			setPull(true);
		}
		@Override
		public RotationAcceleration func(IValue<RotationAcceleration> val, AccelAngle input) {
			Float angleSpeed = input.get();
			val._valueGet().set(angleSpeed > 0f ? angleSpeed : 0f);
//			input.increment();
			return val._valueGet();
		}

		@Override
		public void init(IValue<RotationAcceleration> val) {

		}
	}
	
	public static class Tire extends Filter<Speed, RotationAcceleration, Void> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3198338409546562125L;

		Float tireRadius = 0.5f;
		Float maxSpeed = 200f;
		public Tire() {
			super();
			super._valueSet(new Speed());
		}
		@Override
		public Void func(IValue<Speed> val, RotationAcceleration input) {
			float nextSpeed = val._valueGet().get() + tireRadius*input.get();
			if(nextSpeed < 0f) nextSpeed = 0f;
			else if(nextSpeed > maxSpeed) nextSpeed = maxSpeed;
			val._valueGet().set(nextSpeed);
			val._valueGet().increment();
			return null;
		}

		@Override
		public void init(IValue<Speed> val) {

		}

		@Override
		public boolean _valueSet(Speed value) {
			try {
				((ViewActor)getParent(PathType.FAMILY))._valueSet(value);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			return super._valueSet(value);
		}
		
	}
	
	public static class RepeatRoad extends ValueArrayEffector<IPoint> implements Road {
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
				return lastPoint = _valueGet();
			}
			IPoint newPoint = (nextGoal == null) ? _valueGet() : nextGoal;
			for(float restd = pos - lastPos; ;) {
				IPoint vec = newPoint.subNew(lastPoint);
				float dist = vec.getAbs();
				if(dist > restd) {
					lastPoint = vec.ein().multiply(restd).plus(lastPoint);
					nextGoal = newPoint;
					break;
				} else {
					lastPoint = newPoint;
					newPoint = _valueGet();
					restd -= dist;
				}
			}
			lastPos = pos;
			return lastPoint;
				
		}
		
		@Override
		public void effect(IValue<IPoint> _t, IPoint _e) throws ChainException {
			Speed s = (Speed)_t._valueGet();
			L("1.202 ValueEffector valueSet").go(_t._valueSet(s.set(getPosXY(s.getPos()))));
		}
	}
	
	public static class AccelPedal extends Generator<AccelAngle> {
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
			val._valueSet(new AccelAngle());
		}
	}
	
	public static class BrakePedal extends Generator<BrakeAngle> {
		public BrakePedal() {
			super();
		}
		public BrakePedal(Float f, Boolean hippo) {
			super(new CarEngineer.BrakeAngle(f), hippo);
		}

		@Override
		public void init(IValue<BrakeAngle> val) {
			val._valueSet(new BrakeAngle());
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