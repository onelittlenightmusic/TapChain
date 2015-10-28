package org.tapchain;

import org.tapchain.core.Actor;
import org.tapchain.core.WorldPoint;
@SuppressWarnings("serial")
public class TapChainGoalTap extends ActorTap {
	static int status = 0;
	@Override
	public void view_init() {
		setCenter(new WorldPoint(-300f, -300f));
	}
	@Override
	public int onTick(Actor p, Object obj) {
		status++;
		interrupt(ControllableSignal.END);
		return 1;
	}
	public static String printState() {
		return Integer.toString(status);
	}
}
