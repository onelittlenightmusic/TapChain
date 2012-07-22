package org.tapchain.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;

public class Blueprint implements IBlueprint {
	Class<? extends Actor> cls = null;
	ArrayList<ConnectionBlueprint> connect = new ArrayList<ConnectionBlueprint>();
	ArrayList<Reservation> children = new ArrayList<Reservation>();
	IBlueprint view = null;
	Reservation This = null;
//	HashMap<Class<?>, Object> parent = null;
	Class<?> parent_type;
	Object parent_obj;
	Class<?>[] types;
	Object[] ar;
	
	//1.Initialization
	protected Blueprint() {
	}
	public Blueprint(Class<? extends Actor> _cls, Actor... _args) {
		this();
		setClass(_cls);
		This = newReservation(_args).setParent(this);
	}
	
	//2.Getters and setters
	public Reservation This() {
		return This;
	}
	protected Blueprint setClass(Class<? extends Actor> _cls) {
		cls = _cls;
		return this;
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
	public Reservation newReservation(IPiece... args) {
		return new Reservation(this, args);
	}
	protected Actor __newInstance(Class<?>[] types, Object[] args) throws IllegalAccessException, InstantiationException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		if(args != null)
			return cls.getConstructor(types).newInstance(args);
		return cls.newInstance();
	}
	protected Actor __newInstance() throws IllegalAccessException, InstantiationException {
		return cls.newInstance();
	}
	public Blueprint addArgs(Class<?>[] type, Object[] args) {
		types = type;
		ar = args;
		return this;
	}
	
	public Actor newInstance(PieceManager maker) throws ChainException {
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
		if(types != null) {
			_types.addAll(Arrays.asList(types));
			_obj.addAll(Arrays.asList(ar));
		}
		if(_types.isEmpty())
			return newInstance(maker, null, null);
		return newInstance(maker, _types.toArray(new Class<?>[]{}), _obj.toArray());
	}
	public Actor newInstance(PieceManager maker, Class<?>[] types, Object[] args) throws ChainException {
		Actor rtn = null;
//		ChainException ex = null;
		try {
			rtn = __newInstance(types, args);
			This.setInstance(rtn, maker);
		__init_children(rtn, maker);
		init_user(rtn, maker);
		} catch (Exception e) {
			throw /*ex = */new ChainException(maker, "PieceFactory: cant create new instance");
		}
//		if(ex != null)
//			if(maker != null)
//				maker.error(ex);
		if(maker != null) {
			maker.add(rtn);
		}
		return rtn;
	}
	public void init_user(Actor newinstance, PieceManager maker) throws InterruptedException {
	}
	private Actor __init_children(Actor newinstance, PieceManager maker) throws IllegalAccessException, InstantiationException, ChainException, InterruptedException {
		for(Reservation local: children) {
			local.instantiate(maker);
//			This.getInstance().addMember(local.getInstance());
		}
		for(ConnectionBlueprint cb: connect)
			cb.connect(maker);
		return newinstance;
	}
	public Reservation addLocal(IBlueprint bp, IPiece... args) {
		Reservation rtn = bp.newReservation(args).setParent(this);
		children.add(rtn);
		return rtn;
	}
	public Blueprint append(ConnectionBlueprint conn) {
		connect.add(conn);
		return this;
	}
	public Blueprint Append(PackType stack, Reservation target, PackType stack_target) {
		append(new ConnectionBlueprint(This, target, stack, stack_target));
		return this;
	}
	public Blueprint refresh() {
		This.refresh();
		for(Reservation local: children)
			local.refresh();
		return this;
	}
	
	//4.Termination
	//5.Local classes
	public static class Reservation {
		Blueprint cls = null, parent = null;
		Actor instantiated = null;
		IPiece[] var = null;
		Reservation(Blueprint _cls, IPiece... args) {
			cls = _cls;
			var = args;
		}
		Reservation setParent(Blueprint _parent) {
			parent = _parent;
			return this;
		}
		public Actor instantiate(PieceManager maker) throws IllegalAccessException, InstantiationException, ChainException, InterruptedException {
			return setInstance( cls.newInstance(maker), maker);
		}
		public synchronized Actor setInstance(Actor bp, PieceManager maker) throws ChainException {
			instantiated = bp;
			if(maker != null) {
				maker._return(instantiated);
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
			notifyAll();
			return instantiated;
		}
		public synchronized Actor getInstance() throws InterruptedException {
			while(instantiated == null)
				wait();
			return instantiated;
		}
		public Reservation refresh() {
			instantiated = null;
			return this;
		}
		public Reservation Append(PackType stack, Reservation target, PackType stack_target) {
			parent.append(new ConnectionBlueprint(this, target, stack, stack_target));
			return this;
		}
	}
	
	public static class ConnectionBlueprint {
		Reservation Appender;
		Reservation Appendee;
		PackType appender_pack;
		PackType appendee_pack;
		Blueprint view = null;
		public ConnectionBlueprint(Reservation pieceBlueprint,
				Reservation target, PackType stack, PackType stack_target) {
			Appender = pieceBlueprint;
			Appendee = target;
			appender_pack = stack;
			appendee_pack = stack_target;
		}
		public void connect(PieceManager maker) throws ChainException, InterruptedException, IllegalAccessException, InstantiationException {
			maker.append(Appender.getInstance(), appender_pack, Appendee.getInstance(), appendee_pack);
			if(view != null)
				maker.add(view.newInstance(null));
		}
		public ConnectionBlueprint setview(Blueprint _view) {
			view = _view;
			return this;
		}

	}
	
	public static class PieceBlueprintStatic extends Blueprint {
		Actor instance = null;
		public PieceBlueprintStatic(Actor bp) {
			instance = bp;
		}
		@Override
		public Actor newInstance(PieceManager maker) throws ChainException {
			if(maker != null)
				maker.add(instance);
			return instance;
		}
	}
}
