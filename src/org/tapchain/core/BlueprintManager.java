package org.tapchain.core;

import org.tapchain.core.Blueprint.*;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;


@SuppressWarnings("unchecked")
public class BlueprintManager implements IManager<IBlueprint> {
	ActorManager parentMaker = null;
	IBlueprint root = null;
	Reservation marked = null;
	Reservation reserved = null;
	private Object outer;
	//1.Initialization
	public BlueprintManager() {
	}
	
	//2.Getters and setters
	public BlueprintManager setParent(ActorManager _parent) {
		parentMaker = _parent;
		return this;
	}
	public ActorManager getParent() {
		return parentMaker;
	}
	
	public BlueprintManager Error(ChainException e) {
		if(parentMaker != null)
			parentMaker.error(e);
//		Log.e("Error ACM", e.err);
		return this;
	}
	
	public BlueprintManager setOuterInstanceForInner(Object outer) {
		this.outer = outer;
		return this;
	}
	@Override
	public IManager<IBlueprint> newSession() {
		return this;
	}
	@Override
	public IManager<IBlueprint> log(String... s) {
		return this;
	}
	
	//3.Changing state
	public BlueprintManager New(Class<? extends Actor> _cls, Actor... args) {
		return New(new Blueprint(_cls, args));
	}
	public BlueprintManager addArg(Class<?>[] type, Object[] obj) {
		root.addArgs(type, obj);
		return this;
	}
	public BlueprintManager NewLocal(Class<? extends Actor> _cls, Object parent, Actor... args) {
		return New(new Blueprint(_cls, args).setLocalClass(parent.getClass(), parent));
	}
	public BlueprintManager New(IBlueprint pbp) {
		root = pbp;
		marked = root.This();
		reserved = root.This();
		return this;
	}
	@Override
	public BlueprintManager add(IBlueprint _pbp, IPiece... args) {
		reserved = root.addLocal(_pbp, args);
		return this;
	}
	public BlueprintManager add(Class<? extends Actor> _cls, Actor... args) {
		return add(new Blueprint(_cls), args);
	}
	public Reservation getLast() {
		return reserved;
	}
	@Override
	public BlueprintManager teacher(IBlueprint _pbp, IPiece... args) {
		Reservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.HEAP, reserved, PackType.HEAP);
		return this;
	}
	@Override
	public BlueprintManager young(IBlueprint _pbp, IPiece... args) {
		Reservation past = reserved;
		add(_pbp, args);
		reserved.Append(PackType.PASSTHRU, past, PackType.PASSTHRU);
		return this;
	}
	public BlueprintManager parent(Blueprint _pbp, Actor... args) {
		Reservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.HEAP, reserved, PackType.FAMILY);
		return this;
	}
	public BlueprintManager child(Blueprint _pbp, Actor... args) {
		Reservation past = reserved;
		add(_pbp, args);
		reserved.Append(PackType.PASSTHRU, past, PackType.FAMILY);
		return this;
	}
	
	public BlueprintManager args(Class<? extends Actor> _cls, Actor... args) {
		return teacher(new Blueprint(_cls), args);
	}
	public BlueprintManager and(Class<? extends Actor> _cls, Actor... args) {
		return young(new Blueprint(_cls), args);
	}
	public BlueprintManager parent(Class<? extends Actor> _cls, Actor... args) {
		return parent(new Blueprint(_cls), args);
	}
	public BlueprintManager child(Class<? extends Actor> _cls, Actor... args) {
		return child(new Blueprint(_cls), args);
	}
	public BlueprintManager because(Class<? extends Actor> _cls, Actor... args) {
		return because(new Blueprint(_cls), args);
	}
	public BlueprintManager args(Actor bp, Actor... args) {
		return teacher(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager and(Actor bp, Actor... args) {
		return young(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager parent(Actor bp, Actor... args) {
		return parent(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager child(Actor bp, Actor... args) {
		return child(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager because(Actor bp, Actor... args) {
		return because(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager because(Blueprint _pbp, Actor... args) {
		Reservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.EVENT, reserved, PackType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager save() {
		parentMaker.getFactory().Register(root);
		return this;
	}
	public IBlueprint getBlueprint() {
		return root;
	}
	@Override
	public BlueprintManager _mark() {
		marked = reserved;
		return this;
	}
	@Override
	public BlueprintManager _gomark() {
		reserved = marked;
		return this;
	}
	public BlueprintManager setView(Class<? extends Actor.ViewActor> _cls, Actor... args) {
		if(_cls.isMemberClass())
			root.setView(new Blueprint(_cls, args).setLocalClass(outer.getClass(), outer));
		else
			root.setView(new Blueprint(_cls, args));
		return this;
	}
//	public BlueprintManager setviewLocal(Class<? extends BasicView> _cls, BasicPiece... args) {
//		if(_cls.isMemberClass())
//			root.setview(new PieceBlueprint(_cls, args).setLocalClass(outer.getClass(), outer));
//		return this;
//	}
	/*	@Override
	public BlueprintManager reset(Blueprint _pbp, IPiece... args) {
		Reservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.FAMILY, reserved, PackType.EVENT);
		return this;
	}
*/
	/*	public BlueprintManager reset(Class<? extends Actor> _cls, Actor... args) {
	return reset(new Blueprint(_cls), args);
}
*/
	/*	public BlueprintManager reset(Actor bp, Actor... args) {
	return reset(new PieceBlueprintStatic(bp), args);
}
*/


	@Override
	public IManager<IBlueprint> _return(IBlueprint point) {
		root = point;
		return this;
	}

}
