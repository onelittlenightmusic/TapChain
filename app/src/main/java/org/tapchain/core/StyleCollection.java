package org.tapchain.core;

public class StyleCollection {
	Class<? extends ViewActor> view;
	Class<? extends IPiece> connect;
	IActorSharedHandler eh = null;
	Object outer;
	IActionStyle actionStyle;
	IActorConnectHandler actorConnectHandler;
	public StyleCollection(Object context, Class<? extends ViewActor> pieceview, Class<? extends IPiece> connectview, IActionStyle actionstyle, IActorSharedHandler eh, IActorConnectHandler ch) {
		outer = context;
		view = pieceview;
		connect = connectview;
		actionStyle = actionstyle;
		actorConnectHandler = ch;
		this.eh = eh;
//		try {
//			connectview.getConstructor(outer.getClass(), pieceview, pieceview, PackType.class, PackType.class);
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		}
	}
	public IActorConnectHandler getConnectHandler() { return actorConnectHandler; }
	public Class<? extends IPiece> getConnect() {
		return connect;
	}
	public Class<? extends ViewActor> getView() {
		return view;
	}
	public IActionStyle getActionStyle() {
		return actionStyle;
	}
	public Object getOuter() {
		return outer;
	}
	public IActorSharedHandler getEventHandler() {
		return eh;
	}
}