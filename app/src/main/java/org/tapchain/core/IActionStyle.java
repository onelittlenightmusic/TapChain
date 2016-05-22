package org.tapchain.core;

import org.tapchain.core.ActorChain.IView;
import org.tapchain.editor.TapChain.InteractionType;

public interface IActionStyle {
	public InteractionType checkTouchType(IView v1, IView v2);
	public InteractionType checkLeaveType(IView v1, IView v2);
}