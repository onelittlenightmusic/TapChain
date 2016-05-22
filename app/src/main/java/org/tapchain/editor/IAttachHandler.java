package org.tapchain.editor;

public interface IAttachHandler<EDITOR, ACTOR, VIEW> {
	boolean onTouch(EDITOR edit, VIEW t2, ACTOR a1, ACTOR a2);
}
