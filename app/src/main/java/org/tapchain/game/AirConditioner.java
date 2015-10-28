package org.tapchain.game;

import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.Self;

public class AirConditioner extends Controllable<Self, Void, Void, Void> {
	public AirConditioner() {
		super();
	}
	
	public static class Compresser extends Controllable<Self, Electricity, Air, Void> {
		public Compresser() {
			super();
		}
	}
	
	public static class Air extends MyFloat {
		public Air() { super(); }
		public Air(Float f) { super(f); }
	}
	
	
}
