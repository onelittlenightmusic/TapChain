package org.tapchain.core;

import org.tapchain.core.Actor.ViewActor;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.TapChainEdit.ActionStyle;

public class StyleCollection {
	Class<? extends ViewActor> view;
	Class<? extends IPiece> connect;
	Object outer;
	ActionStyle actionStyle;
	public StyleCollection(Object context, Class<? extends ViewActor> pieceview, Class<? extends IPiece> connectview, ActionStyle actionstyle) {
		outer = context;
		view = pieceview;
		connect = connectview;
		actionStyle = actionstyle;
		try {
			connectview.getConstructor(outer.getClass(), pieceview, pieceview, PackType.class, PackType.class);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	public Class<? extends IPiece> getConnect() {
		return connect;
	}
	public Class<? extends ViewActor> getView() {
		return view;
	}
	public ActionStyle getActionStyle() {
		return actionStyle;
	}
	public Object getOuter() {
		return outer;
	}
}