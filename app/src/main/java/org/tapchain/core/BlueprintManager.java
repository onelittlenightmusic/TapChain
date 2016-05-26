package org.tapchain.core;

import java.lang.reflect.Modifier;

import org.tapchain.core.Blueprint.PieceBlueprintStatic;
import org.tapchain.editor.EditorChain;

import android.util.Log;


public class BlueprintManager<TYPE extends Piece> implements IManager<IBlueprint, IPiece> {
	BlueprintManager<TYPE> parentManager = null;
	private IBlueprint<TYPE> root = null;
	IBlueprint reserved = null;
    IBlueprint marked = null;
	Factory<TYPE> factory = null;
	protected Object outer;
    Chain root_chain;

	//1.Initialization
	public BlueprintManager(Chain c) {
        setChain(c);
	}
	
	public BlueprintManager(Chain c, Factory<TYPE> _factory) {
		this(c);
		factory = _factory;
	}
	
	public BlueprintManager(BlueprintManager<TYPE> bm) {
		this(bm.getChain(), bm.factory);
		setParent(bm);
		setOuterInstanceForInner(bm.outer);
//		defaultView = bm.defaultView;
	}
	
	//2.Getters and setters
	public BlueprintManager<TYPE> setParent(BlueprintManager<TYPE> _parent) {
		parentManager = _parent;
		return this;
	}
	public BlueprintManager<TYPE> getParent() {
		return parentManager;
	}
	
	public Blueprint __create(Class<? extends TYPE> _cls) {
		return new Blueprint(getChain(), _cls);
	}

    public Blueprint __create(Class<? extends TYPE> _cls, Object... outer) {
        if(outer == null)
            return __create(_cls);
        if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
                || _cls.isLocalClass()
                || (_cls.isAnonymousClass() && !Modifier.isStatic(_cls.getModifiers()))) {
            Blueprint bp = __create(_cls);
            for (Object obj : outer)
                bp.addLocalClass(obj.getClass(), obj);
            return bp;
        }
        else
            return __create(_cls);
    }

    public Blueprint create(Class<? extends TYPE> _cls) {
        return __create(_cls, outer);
    }

	/**
	 * @return the root
	 */
	public IBlueprint<TYPE> getRoot() {
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
//		if(outer == null)
//			return add(__create(_cls));
//		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
//				|| _cls.isLocalClass()
//				|| (_cls.isAnonymousClass() && !Modifier.isStatic(_cls.getModifiers()))) {
//			Blueprint bp = __create(_cls);
//			for (Object obj : outer)
//				bp.addLocalClass(obj.getClass(), obj);
//			return add(bp);
//		}
//		else
//			return add(__create(_cls));
        return add(__create(_cls, outer));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BlueprintManager<TYPE> add(IBlueprint pbp) {
		setRoot(pbp);
		marked = getRoot();
		reserved = getRoot();
		return this;
	}
	

//	public static <VALUE, INPUT, OUTPUT> Class<? extends Actor> createBlueprintFromDesigner(final IFunc<VALUE, INPUT, OUTPUT> func, final VALUE init) {
//		if(init instanceof IFunc){
//			class LocalFilter extends Actor.Filter<VALUE, INPUT, OUTPUT> {
//				public LocalFilter() {
//					super(init, IInit.class);
////					_set(init);
//				}
//				@Override
//				public OUTPUT func(IValue<VALUE> val, INPUT in) {
//					return func.func(val, in);
//				}
//
//				@Override
//				public void init(IValue<VALUE> val) {
//					val._set(init);
//				}
//			};
//			return LocalFilter.class;
//		}
//		else if(init instanceof IConsumer) {
//			class LocalConsumer extends Actor.ValueConsumer<VALUE> {
//				public LocalConsumer() {
//					super(init, IDesigner.class);
//				}
//				@Override
//				public void consume(VALUE in) {
//					((IConsumer<VALUE>)init).consume(in);
//				}
//
//				@Override
//				public void init(IValue<VALUE> val) {
//					init.init(val);
//				}
//			};
//			return LocalConsumer.class;
//		}
//		else if(init instanceof IGenerator) {
//			class LocalGenerator extends Actor.Generator<VALUE> {
//				public LocalGenerator() {
//					super(init, IDesigner.class);
//				}
//				@Override
//				public VALUE generate() {
//					return ((IGenerator<VALUE>)init).generate();
//				}
//				@Override
//				public void init(IValue<VALUE> val) {
//					init.init(val);
//				}
//			};
//			return LocalGenerator.class;
//		}
//		return null;
//	}
	
//	public static <PARENT, EFFECT> Actor createEffector(final IEffector<PARENT, EFFECT> effector) {
//		return new Effector.OriginalEffector<PARENT,EFFECT>() {
//			@Override
//			public void effect(PARENT _parent, IValue<EFFECT> _effect_val) throws ChainException {
//				effector.effect(_parent, _effect_val);
//			}
//		};
//	}
	
//    public <PARENT, EFFECT> BlueprintManager<TYPE> addEffector(final IEffector<PARENT, EFFECT> effector) {
//        Class rtn = createEffector(effector).getClass();
//        return addWithOuterObject(rtn);
//	}
	

	public BlueprintManager<TYPE> arg(Object... objs) {
		reserved.addArg(objs);
		return this;
	}

    @Override
	public BlueprintManager<TYPE> view(Object... objs) {
		if(!isSetView() && getChain() instanceof EditorChain)
			setView(((EditorChain)getChain()).getDefaultView().copyAndRenewArg());
		getView().addArg(objs);
		return this;
	}

    @Override
	public BlueprintManager<TYPE> tag(String tag) {
		getRoot().setTag(tag);
		return this;
	}
//	public BlueprintManager<TYPE> setDefaultView(Chain c, Class<? extends ViewActor> _cls) {
//		defaultView = createTapBlueprint(c, _cls);
//		return this;
//	}

	public BlueprintManager<TYPE> addLocal(IBlueprint _pbp) {
		reserved = getRoot().addLocal(_pbp);
		return this;
	}
	public BlueprintManager<TYPE> addLocal(Class<? extends TYPE> _cls) {
		return addLocal(__create(_cls));
	}
	@Override
	public BlueprintManager<TYPE> pullFrom(IBlueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		past.append(PathType.OFFER, reserved, PathType.OFFER);
		return this;
	}

	public BlueprintManager<TYPE> nextPassThru(IBlueprint _pbp) {
		IBlueprint past = reserved;
		addLocal(_pbp);
		reserved.append(PathType.PASSTHRU, past, PathType.PASSTHRU);
		return this;
	}
	public BlueprintManager<TYPE> offerToFamily(Blueprint _pbp) {
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
	
	public BlueprintManager<TYPE> pullFrom(Class<? extends TYPE> _cls) {
		return pullFrom(__create(_cls));
	}
	public BlueprintManager<TYPE> nextEvent(Class<? extends TYPE> _cls) {
		return nextPassThru(__create(_cls));
	}
	public BlueprintManager<TYPE> offerToFamily(Class<? extends TYPE> _cls) {
		return offerToFamily(__create(_cls));
	}
	public BlueprintManager<TYPE> child(Class<? extends TYPE> _cls) {
		return child(__create(_cls));
	}
	public BlueprintManager<TYPE> prevEvent(Class<? extends TYPE> _cls) {
		return prevEvent(__create(_cls));
	}
	public BlueprintManager<TYPE> pullFrom(TYPE bp, TYPE... args) {
		return pullFrom(new PieceBlueprintStatic(getChain(), bp));
	}
	public BlueprintManager<TYPE> and(Actor bp) {
		return nextPassThru(new PieceBlueprintStatic(getChain(), bp));
	}
	public BlueprintManager<TYPE> offerToFamily(Actor bp) {
		return offerToFamily(new PieceBlueprintStatic(getChain(), bp));
	}
	public BlueprintManager<TYPE> child(Actor bp) {
		return child(new PieceBlueprintStatic(getChain(), bp));
	}
    public BlueprintManager<TYPE> prevEvent(Actor bp) {
        return prevEvent(new PieceBlueprintStatic(getChain(), bp));
    }
    public BlueprintManager<TYPE> prevEvent(Blueprint _pbp) {
        IBlueprint past = reserved;
        addLocal(_pbp);
        past.append(PathType.EVENT, reserved, PathType.EVENT);
        return this;
    }
    public BlueprintManager<TYPE> nextEvent(Blueprint _pbp) {
        IBlueprint past = reserved;
        addLocal(_pbp);
        reserved.append(PathType.EVENT, past, PathType.EVENT);
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

	public IBlueprint<TYPE> getBlueprint() {
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

//	public BlueprintManager<TYPE> setSystem(Class<? extends TYPE> _cls) {
//		IBlueprint blueprint;
//		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
//				|| _cls.isLocalClass()
//				|| _cls.isAnonymousClass())
//			blueprint = __create(_cls).addLocalClass(outer.getClass(), outer);
//		else
//			blueprint = __create(_cls);
//		return setView(blueprint);
//	}
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

//	public Blueprint createTapBlueprint(Class<? extends IPiece> _cls) {
//		if((_cls.isMemberClass() && !Modifier.isStatic(_cls.getModifiers()))
//				|| _cls.isLocalClass()
//				|| _cls.isAnonymousClass())
//			return new TapBlueprint(getChain(), _cls).addLocalClass(outer.getClass(), outer);
//		else
//			return new TapBlueprint(getChain(), _cls);
//	}

	
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

    public <VALUE, INPUT, OUTPUT> BlueprintManager<TYPE> add(final IFilter<VALUE, INPUT, OUTPUT> func, final VALUE init) {
        return this;
    };
    public <VALUE, OUTPUT> BlueprintManager<TYPE> add(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
        return this;
    };
    public <VALUE, INPUT> BlueprintManager<TYPE> add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        return this;
    };
    public <PARENT, EFFECT> BlueprintManager<TYPE> add(final IEffector<PARENT, EFFECT> effector, final EFFECT init, int duration) {
        return this;
    }

    @Override
    public void setChain(Chain c) {
        root_chain = c;
    }

    @Override
    public Chain getChain() {
        return root_chain;
    }

}
