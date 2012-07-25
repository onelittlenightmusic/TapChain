package org.tapchain.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;

public class Blueprint implements IBlueprint {
	Class<? extends IPiece> cls = null;
	ArrayList<ConnectionBlueprint> connect = new ArrayList<ConnectionBlueprint>();
	ArrayList<Blueprint> children = new ArrayList<Blueprint>();
	IBlueprint view = null;
	TmpInstance This = null;
//	HashMap<Class<?>, Object> parent = null;
	Class<?> parent_type;
	Object parent_obj;
	ParamArray params = new ParamArray();
//	ArrayList<Class<?>> types;
//	ArrayList<Object> ar;
	IPiece[] var = null;
	
	//1.Initialization
	protected Blueprint() {
		This = new TmpInstance(this).setParent(this);
	}
	public Blueprint(Class<? extends IPiece> _cls, Actor... _args) {
		this();
		setBPClass(_cls);
		var = _args;
	}
	public Blueprint(Blueprint bp, IPiece... args) {
		this();
		setBPClass(bp.getBPClass());
		This = bp.This();
		var = args;
		params = bp.params;
		parent_type = bp.parent_type;
		parent_obj = bp.parent_obj;
		view = bp.view;
		children = bp.children;
		connect = bp.connect;
	}
	
	//2.Getters and setters
	public TmpInstance This() {
		return This;
	}
	protected Blueprint setBPClass(Class<? extends IPiece> _cls) {
		cls = _cls;
		return this;
	}
	protected Class<? extends IPiece> getBPClass() {
		return cls;
	}
	protected Blueprint setLocalClass(Class<?> parent_type, Object parent_obj) {
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
		return this;
	}
	
	//3.Changing state
	public TmpInstance newReservation() {
		return This();
	}
	protected IPiece __newInstance(Class<?>[] types, Object[] args) throws IllegalAccessException, InstantiationException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		if(args != null)
			return cls.getConstructor(types).newInstance(args);
		return cls.newInstance();
	}
	protected IPiece __newInstance() throws IllegalAccessException, InstantiationException {
		return cls.newInstance();
	}
	public IPiece newInstance(IManager<IPiece> maker) throws ChainException {
		return __newInstance(maker);
	}
	protected IPiece __newInstance(IManager<IPiece> maker) throws ChainException {
//		if(parent != null) {
//			Class<?>[] types = new Class<?>[]{};
//			parent.keySet().toArray(types);
//			return newInstance(maker, factory, types, parent.values().toArray());
		ArrayList<Class<?>> _types = new ArrayList<Class<?>>();
		ArrayList<Object> _obj = new ArrayList<Object>();
		if(parent_obj != null) {
//			Log.w("test", String.format("%s, %s", parent_type.getName(), parent_obj));
			_types.add(parent_type);
			_obj.add(parent_obj);
		}
		if(params != null) {
			_types.addAll(params.getClasses());
			_obj.addAll(params.getObjects());
		}
		if(_types.isEmpty())
			return __newInstance(maker, null, null);
		return __newInstance(maker, _types.toArray(new Class<?>[]{}), _obj.toArray());
	}
	public IPiece __newInstance(IManager<IPiece> maker, Class<?>[] types, Object[] args) throws ChainException {
		IPiece rtn = null;
//		ChainException ex = null;
		try {
			rtn = __newInstance(types, args);
			This.setInstance(rtn, maker);
			__init_children(rtn, maker);
			init_user(rtn, maker);
			if(maker != null) {
				maker._return(This.getInstance());
				maker._mark();
				if(var != null)
					for(IPiece b: var) {
//						Log.w("BlueprintInstance", "Args");
						if(b== null)
							continue;
					maker.teacher(b);
//					instantiated.addMember(b);
					maker._gomark();
				}
			}
		} catch (Exception e) {
			throw /*ex = */new ChainException(maker, "PieceFactory: cant create new instance");
		}
		if(maker != null) {
			maker.add(rtn);
		}
		return rtn;
	}
	public void init_user(IPiece rtn, IManager<IPiece> maker) throws InterruptedException {
	}
	private IPiece __init_children(IPiece rtn, IManager<IPiece> maker) throws IllegalAccessException, InstantiationException, ChainException, InterruptedException {
		for(Blueprint local: children) {
			local.__newInstance(maker);
//			This.getInstance().addMember(local.getInstance());
		}
		for(ConnectionBlueprint cb: connect)
			cb.connect(maker);
		return rtn;
	}
	public Blueprint addLocal(IBlueprint bp, IPiece... args) {
		Blueprint rtn = new Blueprint((Blueprint)bp, args);
		children.add(rtn);
		return rtn;
	}
	public Blueprint append(ConnectionBlueprint conn) {
		connect.add(conn);
		return this;
	}
	public Blueprint Append(PackType stack, IBlueprint target, PackType stack_target) {
		append(new ConnectionBlueprint(This, target.This(), stack, stack_target));
		return this;
	}
	public Blueprint refresh() {
		This.refresh();
		for(Blueprint local: children)
			local.refresh();
		return this;
	}
	/** Add argument classes and objects for current blueprint instantiation
	 * @param type Array of argument classes
	 * @param obj Array of argument objects
	 * @return
	 */
	public Blueprint addArg(Class<?> type, Object arg) {
		params.add(type, arg);
		return this;
	}
	
	//4.Termination
	//5.Local classes
	public static class TmpInstance {
		Blueprint blueprint = null, parent = null;
		IPiece instantiated = null;
		TmpInstance(Blueprint _cls) {
			blueprint = _cls;
//			var = args;
		}
		TmpInstance setParent(Blueprint _parent) {
			parent = _parent;
			return this;
		}
		public synchronized IPiece setInstance(IPiece iPiece, IManager<IPiece> maker) throws ChainException {
			instantiated = iPiece;
			notifyAll();
			return instantiated;
		}
		public synchronized IPiece getInstance() throws InterruptedException {
			while(instantiated == null)
				wait();
			return instantiated;
		}
		public TmpInstance refresh() {
			instantiated = null;
			return this;
		}
		public TmpInstance Append(PackType stack, TmpInstance target, PackType stack_target) {
			parent.append(new ConnectionBlueprint(this, target, stack, stack_target));
			return this;
		}
		
	}
	
	public static class ParamArray {
		ArrayList<Class<?>> cls;
		ArrayList<Object> obj;
		public ParamArray() {
			cls = new ArrayList<Class<?>>();
			obj = new ArrayList<Object>();
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
	}
	
	public static class ConnectionBlueprint {
		TmpInstance Appender;
		TmpInstance Appendee;
		PackType appender_pack;
		PackType appendee_pack;
		Blueprint view = null;
		public ConnectionBlueprint(TmpInstance pieceBlueprint,
				TmpInstance target, PackType stack, PackType stack_target) {
			Appender = pieceBlueprint;
			Appendee = target;
			appender_pack = stack;
			appendee_pack = stack_target;
		}
		public void connect(IManager<IPiece> maker) throws ChainException, InterruptedException, IllegalAccessException, InstantiationException {
			((PieceManager) maker).append(Appender.getInstance(), appender_pack, Appendee.getInstance(), appendee_pack);
			if(view != null)
				maker.add(view.newInstance(null));
		}
		public ConnectionBlueprint setview(Blueprint _view) {
			view = _view;
			return this;
		}

	}
	
	public static class PieceBlueprintStatic extends Blueprint {
		IPiece instance = null;
		public PieceBlueprintStatic(IPiece bp) {
			super();
			instance = bp;
		}
		@Override
		public IPiece newInstance(IManager<IPiece> maker) throws ChainException {
			if(maker != null)
				maker.add(instance);
			return instance;
		}
	}
}
