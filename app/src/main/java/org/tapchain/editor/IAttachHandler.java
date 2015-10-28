package org.tapchain.editor;

import org.tapchain.core.IPiece;

public interface IAttachHandler<EDITOR, ACTOR, VIEW> {
	public boolean onInside(EDITOR edit, VIEW t2, ACTOR a1, ACTOR a2);
}
