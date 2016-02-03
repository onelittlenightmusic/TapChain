package org.tapchain.core;

import java.lang.reflect.Modifier;

import org.tapchain.core.Actor.IConsumer;
import org.tapchain.core.Actor.IDesigner;
import org.tapchain.core.Actor.IEffector;
import org.tapchain.core.Actor.IFunc;
import org.tapchain.core.Actor.IGenerator;
import org.tapchain.core.Blueprint.PieceBlueprintStatic;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.actors.ViewActor;

import android.util.Log;


public class BlueprintManager<TYPE extends Piece> implements IManager<IBlueprint, IPiece> {
	BlueprintManager<TYPE> parentManager = null;
	private IBlueprint root = null;
	IBlueprint marked = null;
	IBlueprint reserved = null;
	IBlueprint defaultView = null;
	Factory<TYPE> factory = null;
	protected Object outer;
	//1.Initialization
	public BlueprintManager() {
	}
	
	public BlueprintManager(Factory<TYPE> _factory) {
		this();
		factory = _factory;
	}
	
	public BlueprintManager(BlueprintManager<TYPE> bm) {
		this(bm.factory);
		setParent(bm);
		setOuterInstanceForInner(bm.outer);
		defaultView = bm.defaultView;
	}
	
	//2.Getters and setters
	public BlueprintManager<TYPE> setParent(BlueprintManager<TYPE> _parent) {
		parentManager = _parent;
		return this;
	}
	public BlueprintManager<TYPE> getParent() {
		return parentManager;
	}
	
	public Blueprint create() {
		return new Blueprint();
	}

	public Blueprint create(Blueprint bp, TYPE... args) {
		return new Blueprint(bp, args);
	}

	public Blueprint create(Class<? extends TYPE> _cls) {
		return new Blueprint(_cls);
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
	public BlueprintManager<TYPE> setRoot(IBlueprint root) {
		this.root = root;
		return this;
	}

	
	public BlueprintManager<TYPE> setOuterInstanceForInner(Object outer) {
		this.outer = outer;
		return this;
	}
	
	@Override
	public BlueprintManager<TYPE> newSession() {
		return new BlueprintManager<TYPE>(this);
	}
	
	public BlueprintManager<TYPE> _in() {
		return newSession().setRoot(root);
	}

	@Override
	public IManager<IBlueprint, IPiece> _in(IBlueprint piece) {
		return null;
	}

	@Override
	public void remove(IBlueprint bp) {
		return;
	}

	@Override
	public IManager<IBlueprint, IPiece> log(String... s) {
		return this;
	}
	
	//3.Changing state
	/** Adding Actor class to BlueprintManager to reproduce Actor instances
	 * @param _cls Actor class
	 * @return BlueprintManagers
	 */
	public BlueprintManager<TYPE> add(Class<? extends TYPE> _cls) {
		return addWithOuterObject(_cls, outer);
	}

    public BlueprintManager<TYPE> add(Class<? extends TYPE> _cls, Object... args) {
        return addWithOuterObject(_cls, outer).arg(args);
    }

    public BlueprintManager<TYPE> addWithOuterObject(Class<? extends TYPE> _cls, Object... outer) {
		if(outer == null)
			return add(create(_cls));
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| (_cls.isAnonymousClass() && !Modifier.isStatic(_cls.getModifiers()))) {
			Blueprint bp = create(_cls);
			for (Object obj : outer)
				bp.addLocalClass(obj.getClass(), obj);
			return add(bp);
		}
		else
			return add(create(_cls));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BlueprintManager<TYPE> add(IBlueprint pbp) {
		setRoot(pbp);
		marked = getRoot();
		reserved = getRoot();
		return this;
	}
	

	public static <VALUE, INPUT, OUTPUT> Class<? extends Actor> createFromDesigner(final IDesigner<VALUE, INPUT, OUTPUT> designer) {
		if(designer instanceof IFunc){
			class LocalFilter extends Actor.Filter<VALUE, INPUT, OUTPUT> {
				public LocalFilter() {
					super(designer, IDesigner.class);
//					_valueSet(init);
				}
				@Override
				public OUTPUT func(IValue<VALUE> val, INPUT in) {
					return ((IFunc<VALUE, INPUT, OUTPUT>)designer).func(val, in);
				}

				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}
			};
			return LocalFilter.class;
		}
		else if(designer instanceof IConsumer) {
			class LocalConsumer extends Actor.ValueConsumer<VALUE> {
				public LocalConsumer() {
					super(designer, IDesigner.class);
				}
				@Override
				public void consume(VALUE in) {
					((IConsumer<VALUE>)designer).consume(in);
				}

				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}
			};
			return LocalConsumer.class;
		}
		else if(designer instanceof IGenerator) {
			class LocalGenerator extends Actor.Generator<VALUE> {
				public LocalGenerator() {
					super(designer, IDesigner.class);
				}
				@Override
				public VALUE generate() {
					return ((IGenerator<VALUE>)designer).generate();
				}
				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}
			};
			return LocalGenerator.class;
		}
		return null;
	}
	
	public static <PARENT, EFFECT> Actor createEffector(final IEffector<PARENT, EFFECT> effector) {
		return new Actor.OriginalEffector<PARENT,EFFECT>() {
			@Override
			public void effect(PARENT _t, EFFECT _e) throws ChainException {
				effector.effect(_t, _e);
			}
		};
	}
	
	@SuppressWarnings("serial")
	public <VALUE, INPUT, OUTPUT> BlueprintManager<TYPE> add(final IDesigner<VALUE, INPUT, OUTPUT> designer) {
		Class rtn = createFromDesigner(designer);
		addWithOuterObject(rtn, designer);
		return this;
	}
	
	public <PARENT, EFFECT> BlueprintManager<TYPE> addEffector(final IEffector<PARENT, EFFECT> effector) {
        Class rtn = createEffector(effector).getClass();
        return addWithOuterObject(rtn);
	}
	

	public BlueprintManager<TYPE> arg(Object... objs) {
		reserved.addArg(objs);
		return this;
	}
	public BlueprintManager<TYPE> setViewArg(Object... objs) {
		if(!isSetView())
			setView(defaultView.copyAndRenewArg());
		getView().addArg(objs);
		return this;
	}
	
	public BlueprintManager<TYPE> setTag(String tag) {
		getRoot().setTag(tag);
		return this;
	}
	public BlueprintManager<TYPE> setDefaultView(Class<? extends ViewActor> _cls) {
		defaultView = createTapBlueprint(_cls);
		return this;
	}

	public BlueprintManager<TYPE> addLocal(IBlueprint _pbp) {
		reserved = getRoot().addLocal(_pbp);
		return this;
	}
	public BlueprintManager<TYPE> addLocal(Class<? extends TYPE> _cls) {
		return addLocal(create(_cls));
	}
	@Override
	public BlueprintManager<TYPE> teacher(IBlueprint _pbp, IPiece... args) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PathType.OFFER, reserved, PathType.OFFER);
		return this;
	}
	@Override
	public BlueprintManager<TYPE> next(IBlueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		reserved.append(PathType.PASSTHRU, past, PathType.PASSTHRU);
		return this;
	}
	public BlueprintManager<TYPE> parent(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PathType.OFFER, reserved, PathType.FAMILY);
		return this;
	}
	public BlueprintManager<TYPE> child(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		reserved.append(PathType.FAMILY, past, PathType.FAMILY);
		return this;
	}
	
	public BlueprintManager<TYPE> teacher(Class<? extends TYPE> _cls) {
		return teacher(create(_cls));
	}
	public BlueprintManager<TYPE> young(Class<? extends TYPE> _cls) {
		return next(create(_cls));
	}
	public BlueprintManager<TYPE> parent(Class<? extends TYPE> _cls) {
		return parent(create(_cls));
	}
	public BlueprintManager<TYPE> child(Class<? extends TYPE> _cls) {
		return child(create(_cls));
	}
	public BlueprintManager<TYPE> because(Class<? extends TYPE> _cls) {
		return because(create(_cls));
	}
	public BlueprintManager<TYPE> teacher(TYPE bp, TYPE... args) {
		return teacher(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager<TYPE> and(Actor bp) {
		return next(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager<TYPE> parent(Actor bp) {
		return parent(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager<TYPE> child(Actor bp) {
		return child(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager<TYPE> because(Actor bp) {
		return because(new PieceBlueprintStatic(bp));
	}
	public BlueprintManager<TYPE> because(Blueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PathType.EVENT, reserved, PathType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager<TYPE> save() {
		factory.Register(getRoot());
		return this;
	}

	public Factory<TYPE> getFactory() {
		return factory;
	}

	public IBlueprint getBlueprint() {
		return getRoot();
	}
	@Override
	public BlueprintManager<TYPE> _mark() {
		marked = reserved;
		return this;
	}
	@Override
	public BlueprintManager<TYPE> _gotomark() {
		reserved = marked;
		return this;
	}

	public BlueprintManager<TYPE> setSystem(Class<? extends TYPE> _cls) {
		IBlueprint blueprint;
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			blueprint = create(_cls).addLocalClass(outer.getClass(), outer);
		else
			blueprint = create(_cls);
		return setView(blueprint);
	}
	public BlueprintManager<TYPE> setView(IBlueprint view) {
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
	public IManager<IBlueprint, IPiece> _move(IBlueprint point) {
		setRoot(point);
		return this;
	}

	@Override
	public IManager<IBlueprint, IPiece> error(ChainException e) {
		return null;
	}

	@Override
	public BlueprintManager<TYPE> _out() {
		if(parentManager != null) return parentManager;
		return this;
	}

	public Blueprint createTapBlueprint(Class<? extends IPiece> _cls) {
		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
				|| _cls.isLocalClass()
				|| _cls.isAnonymousClass())
			return new TapBlueprint(_cls).addLocalClass(outer.getClass(), outer);
		else
			return new TapBlueprint(_cls);
	}

	
	public void log(String tag, String l) {
		Log.w(tag, l);
	}

    public BlueprintManager<TYPE> setLogLevel() {
        return this;
    }

	IErrorHandler error = null;
	public BlueprintManager<TYPE> setError(IErrorHandler handle) {
		error = handle;
		return this;
	}
}
