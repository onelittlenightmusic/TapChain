package org.tapchain.core;

import java.lang.reflect.Modifier;

import org.tapchain.core.Blueprint.*;
import org.tapchain.core.Chain.PackType;


public class BlueprintManager extends Manager<IBlueprint> {
	BlueprintManager parentMaker = null;
	private IBlueprint root = null;
	IBlueprint marked = null;
	IBlueprint reserved = null;
	IBlueprint defaultView = null;
	Factory<IPiece> factory = null;
	private Object outer;
	//1.Initialization
	public BlueprintManager() {
	}
	
	public BlueprintManager(Factory<IPiece> _factory) {
		this();
		factory = _factory;
	}
	
	//2.Getters and setters
	public BlueprintManager setParent(BlueprintManager _parent) {
		parentMaker = _parent;
		return this;
	}
	public BlueprintManager getParent() {
		return parentMaker;
	}
	
	/**
	 * @return the root
	 */
	public IBlueprint getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public BlueprintManager setRoot(IBlueprint root) {
		this.root = root;
		return this;
	}

	
	public BlueprintManager setOuterInstanceForInner(Object outer) {
		this.outer = outer;
		return this;
	}
	@Override
	public BlueprintManager newSession() {
		return new BlueprintManager(factory).setOuterInstanceForInner(outer);
	}
	public BlueprintManager _child() {
		return newSession().setRoot(root);
	}
	@Override
	public IManager<IBlueprint> log(String... s) {
		return this;
	}
	
	//3.Changing state
	public BlueprintManager New(Class<? extends Actor> _cls, Actor... args) {
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			return New(new Blueprint(_cls, args).setLocalClass(outer.getClass(), outer));
		else
			return New(new Blueprint(_cls, args));
	}
	public BlueprintManager New(IBlueprint pbp) {
		setRoot(pbp);
		marked = getRoot();
		reserved = getRoot();
		return this;
	}
	public BlueprintManager arg(Object... objs) {
		reserved.addArg(objs);
		return this;
	}
	public BlueprintManager viewStyleArg(Object... objs) {
		if(!isSetView())
			setView(defaultView.copyAndRenewParam());
		getView().addArg(objs);
		return this;
	}
	public BlueprintManager setDefaultView(Class<? extends Actor.ViewActor> _cls) {
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			defaultView = new Blueprint(_cls).setLocalClass(outer.getClass(), outer);
		else
			defaultView = new Blueprint(_cls);
		return this;
	}

	public BlueprintManager addLocal(IBlueprint _pbp, IPiece... args) {
		reserved = getRoot().addLocal(_pbp, args);
		return this;
	}
	public BlueprintManager addLocal(Class<? extends Actor> _cls, Actor... args) {
		return addLocal(new Blueprint(_cls, args));
	}
	public IBlueprint getLast() {
		return reserved;
	}
	@Override
	public BlueprintManager teacher(IBlueprint _pbp, IPiece... args) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.Append(PackType.HEAP, reserved, PackType.HEAP);
		return this;
	}
	@Override
	public BlueprintManager young(IBlueprint _pbp, IPiece... args) {
		IBlueprint past = reserved;
		addLocal(_pbp, args);
		reserved.Append(PackType.PASSTHRU, past, PackType.PASSTHRU);
		return this;
	}
	public BlueprintManager parent(Blueprint _pbp, Actor... args) {
		IBlueprint past = reserved;
		addLocal(_pbp, args);
		past.Append(PackType.HEAP, reserved, PackType.FAMILY);
		return this;
	}
	public BlueprintManager child(Blueprint _pbp, Actor... args) {
		IBlueprint past = reserved;
		addLocal(_pbp, args);
		reserved.Append(PackType.FAMILY, past, PackType.FAMILY);
		return this;
	}
	
	public BlueprintManager teacher(Class<? extends Actor> _cls, Actor... args) {
		return teacher(new Blueprint(_cls), args);
	}
	public BlueprintManager young(Class<? extends Actor> _cls, Actor... args) {
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
	public BlueprintManager teacher(Actor bp, Actor... args) {
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
		IBlueprint past = reserved;
		addLocal(_pbp, args);
		past.Append(PackType.EVENT, reserved, PackType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager save() {
		factory.Register(getRoot());
		return this;
	}
	public IBlueprint getBlueprint() {
		return getRoot();
	}
	@Override
	public BlueprintManager _mark() {
		marked = reserved;
		return this;
	}
	@Override
	public BlueprintManager _gotomark() {
		reserved = marked;
		return this;
	}
	public BlueprintManager setView(Class<? extends Actor.ViewActor> _cls, Actor... args) {
		IBlueprint blueprint;
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			blueprint = new Blueprint(_cls, args).setLocalClass(outer.getClass(), outer);
		else
			blueprint = new Blueprint(_cls, args);
		return setView(blueprint);
	}
	public BlueprintManager setView(IBlueprint view) {
		getRoot().setView(view);
		reserved = view;
		return this;
	}
	public boolean isSetView() {
		return getRoot().getView() != null;
	}
	public IBlueprint getView() {
		return getRoot().getView();
	}
	@Override
	public IManager<IBlueprint> _return(IBlueprint point) {
		setRoot(point);
		return this;
	}



}
