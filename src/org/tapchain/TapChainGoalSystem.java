package org.tapchain;

import org.tapchain.TapChainAndroidEdit.EditorView;
@SuppressWarnings("serial")
public class TapChainGoalSystem extends EditorView {
	static int status = 0;
	@Override
	public void onTick() {
		status++;
		interrupt(ControllableSignal.END);
	}
	public static String printState() {
		return Integer.toString(status);
	}
}
