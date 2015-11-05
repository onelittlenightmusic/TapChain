package org.tapchain;

import org.tapchain.core.Actor.FloatFilter;
import org.tapchain.core.IValue;
import org.tapchain.core.IValueLog;

public class QueueingTheory {
	public static class Processor extends FloatFilter implements IValueLog {
		float lastInput = 0f;
		float lastOutput = 0f;
		int count = 0;
		float intervalSum = 0f;
		float intervalSquare = 0f;
		public Processor() {
			super();
		}

		@Override
		public Float func(IValue<Float> val, Float in) {
			count ++;
			float elapsedTime = - _valueGet() * (float)Math.log(Math.random());
			if(lastOutput < in) {
				lastInput = in;
				lastOutput = in + elapsedTime;
			} else {
				lastInput = in;
				lastOutput = lastOutput + elapsedTime;
			}
			float interval = lastOutput - lastInput;
			intervalSum += interval;
			intervalSquare += interval*interval;
			return lastOutput;
		}
		
		public Float getAverage() {
			return intervalSum / (float)count;
		}

		public Float getSquare() {
			float ave = getAverage();
			return (float)Math.sqrt((intervalSquare - ave*ave)/(float)count);
		}

		@Override
		public Object _valueLog() {
			return String.format("Average: %.2f\nSquare: %.2f", getAverage(), getSquare());
		}
	}
}
