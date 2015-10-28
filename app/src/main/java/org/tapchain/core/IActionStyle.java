package org.tapchain.core;

import org.tapchain.core.ActorChain.IView;
import org.tapchain.editor.TapChainEditor.InteractionType;

public interface IActionStyle {
	public InteractionType checkTouchType(IView v1, IView v2, boolean onlyInclude);
	public InteractionType checkLeaveType(IView v1, IView v2);
	public IPoint pointOnAdd(IPoint iPoint);
}