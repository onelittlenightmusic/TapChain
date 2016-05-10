package org.tapchain.core;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Blueprint<PIECE extends IPiece> implements IBlueprint<PIECE>, JSONSerializable {
	Class<? extends PIECE> cls = null;
	ArrayList<ConnectionBlueprint> connect = new ArrayList<>();
	ArrayList<IBlueprint> children = new ArrayList<>();
	IBlueprint view = null;
	TmpInstance<PIECE> This = null;
	Class<?> parent_type;
	Object parent_obj;
	ParamArray params = new ParamArray();
	PIECE[] var = null;
	String tag = "";
	IBlueprintFocusNotification notif;
    Chain root_chain;

	// 1.Initialization
	protected Blueprint(Chain c) {
		This = new TmpInstance(this).setParent(this);
        setRootChain(c);
	}

	public Blueprint(Chain c, Class<? extends PIECE> _cls, PIECE... _args) {
		this(c);
		setBlueprintClass(_cls);
		setVar(_args);
	}

    /**
     * Copy construction method.
     * @param bp
     * @param args
     */
	public Blueprint(Blueprint bp, PIECE... args) {
		this(bp.getRootChain());
		setBlueprintClass(bp.getBlueprintClass());
		This = bp.This();
		setVar(args);
		params = bp.params.copy();
		parent_type = bp.parent_type;
		parent_obj = bp.parent_obj;
		view = bp.view;
		children = bp.children;
		connect = bp.connect;
	}

    public void setRootChain(Chain c) {
        root_chain = c;
    }

    @Override
    public Chain getRootChain() {
        return root_chain;
    }

	/**
	 * Add argument classes and objects for current blueprint instantiation
	 * 
	 * @param objs
	 *            Array of argument objects
	 * @return
	 */
	public Blueprint addArg(Object... objs) {
		for(Object obj : objs)
			if(obj != null)
			params.add(obj.getClass(), obj);
		return this;
	}

	private IBlueprint renewArg() {
		params = new ParamArray();
		return this;
	}

	@Override
	public IBlueprint copy() {
		return new Blueprint(this);
	}
	
	@Override
	public IBlueprint copyAndRenewArg() {
		return ((Blueprint)copy()).renewArg();
	}

	// 2.Getters and setters
	public TmpInstance<PIECE> This() {
		return This;
	}

		protected Blueprint setVar(PIECE... args) {
			var = args;
			return this;
		}

	protected Blueprint setBlueprintClass(Class<? extends PIECE> _cls) {
		cls = _cls;

		return this;
	}
	
	@Override
	public Class<? extends IPiece> getBlueprintClass() {
		return cls;
	}
	

	protected Blueprint addLocalClass(Class<?> parent_type, Object parent_obj) {
		this.parent_type = parent_type;
		this.parent_obj = parent_obj;
		return this;
	}

	public String getName() {
		return cls.getCanonicalName();
	}

	public IBlueprint getView() {
		return view;
	}

	public IBlueprint setView(IBlueprint _view) {
		view = _view;
		if(tag != null)
			view.setTag(tag);
		return this;
	}

    protected PIECE __newRawInstance(Class<?>[] types, Object[] args)
			throws IllegalAccessException, InstantiationException,
			IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException {
		if (args != null)
			A:for(Constructor c: cls.getConstructors()) {
				Class[] ts = c.getParameterTypes();
				if(ts.length != types.length) {
//                    log("Not assignable %d->%d", ts.length, types.length);
                    continue A;
                }
				for(int i = 0; i < ts.length; i++) {
//					Class<?> t = (Class<?>)ts[i];
					Class<?> c1 = types[i];
					if(!ClassLib.checkAssignability(ts[i], c1)) {
                        log("Not assignable %s->%s", ts[i].getSimpleName(), c1.getSimpleName());
                        continue A;
                    }
				}
				return (PIECE) c.newInstance(args);
			}
		return cls.newInstance();
	}



	public PIECE __newInstance(Class<?>[] types, Object[] args) throws ChainException {
		PIECE rtn = null;
		try {
            IManager<PIECE, PIECE> am = null;
//            if(maker != null)
                am = (IManager<PIECE, PIECE>)new ActorManager(getRootChain());
			rtn = __newRawInstance(types, args);
//			Log.w("test", "Instance tag: "+ getTag());
			rtn.setTag(getTag());
			This.setInstance(rtn);
			__init_children(rtn, am);
			init_user(rtn, am);

            if(am != null && rtn != null && getRootChain() != null)
                Log.w("test", String.format("%s/%s: ADDED %s", am.getChain().getName(), getRootChain().getName(), rtn.getTag()));
			if (am != null) {
//                IManager<PIECE, PIECE> am = maker;
                am._move(This.getInstance());
                am._mark();
                if (var != null)
                    for (PIECE b : var) {
                        if (b == null)
                            continue;
                        am.pullFrom(b);
                        am._gotomark();
                    }
//			}
//                if (am != null)
              am.add(rtn).save();
            }
		} catch (Exception e) {
			e.printStackTrace();
			throw /* ex = */new ChainException("PieceFactory: cant create new instance");
		}
//		if (maker != null) {
//			maker.add(rtn);
//		}
		return rtn;
	}

	protected PIECE __newInstance()
			throws ChainException {
		ArrayList<Class<?>> _types = new ArrayList<>();
		ArrayList<Object> _obj = new ArrayList<>();
		if (parent_obj != null) {
			_types.add(parent_type);
			_obj.add(parent_obj);
		}
		if (params != null) {
			_types.addAll(params.getClasses());
			_obj.addAll(params.getObjects());
		}
		PIECE rtn;
		if (_types.isEmpty())
			rtn = __newInstance(null, null);
        else
    		rtn = __newInstance(_types.toArray(new Class<?>[] {}),
				_obj.toArray());
		return rtn;
	}

	public PIECE newInstance() throws ChainException {
		return __newInstance();
	}


	public void init_user(IPiece rtn, IManager<PIECE, PIECE> maker)
			throws InterruptedException {
		for(IBlueprintInitialization ini: inits)
			ini.init(rtn);
	}

	private IPiece __init_children(IPiece rtn, IManager<PIECE, PIECE> maker)
			throws IllegalAccessException, InstantiationException,
			ChainException, InterruptedException {
		for (IBlueprint local : children) {
			local.newInstance();
		}
		for (ConnectionBlueprint cb : connect)
			cb.connect();
		return rtn;
	}

	public IBlueprint addLocal(IBlueprint bp) {
		IBlueprint rtn = bp.copy();
		children.add(rtn);
		return rtn;
	}

	public Blueprint append(ConnectionBlueprint conn) {
		connect.add(conn);
		return this;
	}

	public Blueprint append(PathType stack, IBlueprint target,
			PathType stack_target) {
		append(new ConnectionBlueprint(this, target, stack, stack_target));
		return this;
	}

	public Blueprint refresh() {
		This.refresh();
		for (IBlueprint local : children)
			local.refresh();
		return this;
	}

	// 4.Termination
	// 5.Local classes
	public static class TmpInstance<T extends IPiece> {
		Blueprint blueprint = null, parent = null;
		T instantiated = null;

		TmpInstance(Blueprint _cls) {
			blueprint = _cls;
		}

		TmpInstance setParent(Blueprint _parent) {
			parent = _parent;
			return this;
		}

		public synchronized T setInstance(T iPiece)
				throws ChainException {
			instantiated = iPiece;
			notifyAll();
			return instantiated;
		}

		public synchronized T getInstance() throws InterruptedException {
			while (instantiated == null)
				wait();
			return instantiated;
		}

		public TmpInstance refresh() {
			instantiated = null;
			return this;
		}

	}

	public static class ParamArray {
		ArrayList<Class<?>> cls;
		ArrayList<Object> obj;

		public ParamArray() {
			cls = new ArrayList<>();
			obj = new ArrayList<>();
		}

		public void add(Class<?> _cls, Object _obj) {
			cls.add(_cls);
			obj.add(_obj);
		}

		public ArrayList<Class<?>> getClasses() {
			return cls;
		}

		public ArrayList<Object> getObjects() {
			return obj;
		}
		
		public ParamArray copy() {
			ParamArray pa = new ParamArray();
			pa.cls.addAll(cls);
			pa.obj.addAll(obj);
			return pa;
		}
	}

	public static class ConnectionBlueprint<PIECE extends Piece> {
		IBlueprint<PIECE> Appender;
		IBlueprint<PIECE> Appendee;
		PathType appender_pack;
		PathType appendee_pack;
		IBlueprint<PIECE> view = null;

		public ConnectionBlueprint(IBlueprint pieceBlueprint,
				IBlueprint target, PathType stack, PathType stack_target) {
			Appender = pieceBlueprint;
			Appendee = target;
			appender_pack = stack;
			appendee_pack = stack_target;
		}

		public void connect() throws ChainException,
				InterruptedException, IllegalAccessException,
				InstantiationException {
			new PieceManager<PIECE>()
					.append(Appender.This().getInstance(), appender_pack,
							Appendee.This().getInstance(), appendee_pack, false);
			if (view != null)
//				maker.add(view.newInstance());
                view.newInstance();
		}

		public ConnectionBlueprint setview(Blueprint _view) {
			view = _view;
			return this;
		}

	}

	public static class PieceBlueprintStatic<T extends IPiece> extends Blueprint<T> {
		T instance = null;

		public PieceBlueprintStatic(Chain c, T bp) {
			super(c);
			instance = bp;
		}

		public PieceBlueprintStatic(Blueprint bp, T... args) {
			super(bp, args);
			instance = ((PieceBlueprintStatic<T>) bp).instance;
		}

		public IBlueprint copy(T... args) {
			return new PieceBlueprintStatic(this).setVar(args);
		}

		@Override
		protected T __newRawInstance(Class<?>[] types, Object[] args) {
			return instance;
		}
	}

	@Override
	public void setTag(String tag) {
		this.tag = tag;
		if(view != null)
			view.setTag(tag+"view");
	}

	@Override
	public String getTag() {
		return tag;
	}

	ArrayList<IBlueprintInitialization> inits = new ArrayList<>();
	@Override
	public void setInitialization(IBlueprintInitialization i) {
		inits.add(i);
	}
	
	LinkBooleanSet connectSet = new LinkBooleanSet();
	@Override
	public boolean getFocused(LinkType ac) {
		return connectSet.isTrue(ac);
	}
	
	@Override
	public void highlight(LinkType ac) {
		connectSet = ac.getBooleanSet();
		if(notif != null) {
			notif.onFocus(connectSet);
		}
	}

	@Override
	public void unhighlight() {
		if(notif != null) {
			notif.onFocus(LinkBooleanSet.NULL);
		}
	}

	@Override
	public void setNotification(IBlueprintFocusNotification n) {
		notif = n;
        notif.onFocus(connectSet);
	}
	
	public void log(String format, Object...l) {
//        Log.w("test", String.format(format, l));
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject rtn = new JSONObject();
		rtn.put("Tag", getTag());
		rtn.put("Class", cls.getName());
		return rtn;
	}
}
