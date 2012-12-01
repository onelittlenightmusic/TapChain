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
	public BlueprintManager add(Class<? extends Actor> _cls) {
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			return add(new Blueprint(_cls).setLocalClass(outer.getClass(), outer));
		else
			return add(new Blueprint(_cls));
	}
	@Override
	public BlueprintManager add(IBlueprint pbp) {
		setRoot(pbp);
		marked = getRoot();
		reserved = getRoot();
		factory.Register(getRoot());
		return this;
	}
	public BlueprintManager arg(Object... objs) {
		reserved.addArg(objs);
		return this;
	}
	public BlueprintManager setSystemArg(Object... objs) {
		if(!isSetView())
			setView(defaultView.copyAndRenewArg());
		getView().addArg(objs);
		return this;
	}
	public BlueprintManager setDefaultView(Class<? extends Actor.ViewActor> _cls) {
		defaultView = createBlueprint(_cls);
		return this;
	}

	public BlueprintManager addLocal(IBlueprint _pbp) {
		reserved = getRoot().addLocal(_pbp);
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
		past.append(PackType.HEAP, reserved, PackType.HEAP);
		return this;
	}
	@Override
	public BlueprintManager young(IBlueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		reserved.append(PackType.PASSTHRU, past, PackType.PASSTHRU);
		return this;
	}
	public BlueprintManager parent(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PackType.HEAP, reserved, PackType.FAMILY);
		return this;
	}
	public BlueprintManager child(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		reserved.append(PackType.FAMILY, past, PackType.FAMILY);
		return this;
	}
	
	public BlueprintManager teacher(Class<? extends Actor> _cls) {
		return teacher(new Blueprint(_cls));
	}
	public BlueprintManager young(Class<? extends Actor> _cls) {
		return young(new Blueprint(_cls));
	}
	public BlueprintManager parent(Class<? extends Actor> _cls) {
		return parent(new Blueprint(_cls));
	}
	public BlueprintManager child(Class<? extends Actor> _cls) {
		return child(new Blueprint(_cls));
	}
	public BlueprintManager because(Class<? extends Actor> _cls) {
		return because(new Blueprint(_cls));
	}
	public BlueprintManager teacher(Actor bp, Actor... args) {
		return teacher(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager and(Actor bp) {
		return young(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager parent(Actor bp) {
		return parent(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager child(Actor bp) {
		return child(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager because(Actor bp) {
		return because(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager because(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PackType.EVENT, reserved, PackType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager _save() {
//		factory.Register(getRoot());
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
	public BlueprintManager setSystem(Class<? extends Actor.ViewActor> _cls, Actor... args) {
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

	public Blueprint createBlueprint(Class<? extends IPiece> _cls) {
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			return new Blueprint(_cls).setLocalClass(outer.getClass(), outer);
		else
			return new Blueprint(_cls);
	}

}
